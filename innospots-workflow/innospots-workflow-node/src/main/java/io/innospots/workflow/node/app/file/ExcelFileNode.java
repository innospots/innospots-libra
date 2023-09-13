package io.innospots.workflow.node.app.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.google.common.collect.Lists;
import io.innospots.base.condition.Factor;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.ExecutionResource;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Smars
 * @date 2023/9/1
 */
@Slf4j
public class ExcelFileNode extends BaseAppNode {

    private static final String FILE_OPT = "operation";

    private static final String FILE_PATH = "file_path";

    private static final String FILE_FORMAT = "file_format";
    private static final String LINE_SEPARATOR = "separator";
    private static final String HAS_FIRST_HEADER = "first_header";

    private static final String MAX_READ_LINE = "max_read_line";

    private static final String IS_SELECT_COLUMN = "is_select_column";

    private static final String SELECT_FIELDS = "select_fields";
    private static final String ENCODING = "encoding";

    /**
     * operate excel file, eg: read or write
     */
    private FileOperation fileOperation;


    /**
     * the excel file absolute path
     */
    private String filePath;

    /**
     * csv or xls, xlsx
     */
    private ExcelTypeEnum fileFormat;

    /**
     * line column separator in csv file
     */
    private String lineSeparator;

    /**
     * if true the first line is the column name
     */
    private boolean firstHeader;

    /**
     * read max lines when read the excel
     */
    private Integer maxReadLines;

    private boolean selectColumn;

    /**
     * select fields
     */
    private List<ParamField> selectedFields;

    private Charset encoding;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        this.fileOperation = FileOperation.valueOf(this.valueString(FILE_OPT));
        this.filePath = this.valueString(FILE_PATH);
        this.fileFormat = ExcelTypeEnum.valueOf(this.valueString(FILE_FORMAT).toUpperCase());
        if (fileFormat == ExcelTypeEnum.CSV) {
            this.lineSeparator = this.valueString(LINE_SEPARATOR);
        }
        encoding = Charset.forName(nodeInstance.valueString(ENCODING));
        this.selectColumn = nodeInstance.valueBoolean(IS_SELECT_COLUMN);
        maxReadLines = nodeInstance.valueInteger(MAX_READ_LINE);
        firstHeader = nodeInstance.valueBoolean(HAS_FIRST_HEADER);
        List<Map<String, Object>> v = (List<Map<String, Object>>) nodeInstance.value(SELECT_FIELDS);
        if (v != null) {
            selectedFields = BeanUtils.toBean(v, ParamField.class);
        }

    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        switch (fileOperation) {
            case READ:
                read(nodeExecution);
                break;
            case WRITE:
                write(nodeExecution);
                break;
            default:
                break;
        }
    }


    private void read(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = buildOutput(nodeExecution);
        Map<Integer, String> header = new LinkedHashMap<>();
        AtomicInteger counter = new AtomicInteger();
        log.info("read file: {}",filePath);

        ExcelReaderBuilder readerBuilder = EasyExcel.read(filePath, new ReadListener<Map<Integer, Object>>() {

            @Override
            public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                ReadListener.super.invokeHead(headMap, context);
                if (header.isEmpty() && firstHeader) {
                    headMap.forEach((k,v)->{
                        header.put(k,v.getStringValue());
                    });
                    log.info("header column:{}",header);
                }
            }

            @Override
            public void invoke(Map<Integer, Object> item, AnalysisContext analysisContext) {

                if (maxReadLines != null && counter.get() >= maxReadLines) {
                    return;
                }
                Map<String, Object> nItem = new LinkedHashMap<>();
                if (selectColumn && selectedFields != null) {
                    for (ParamField field : selectedFields) {
                        nItem.put(field.getCode(), item.getOrDefault(field.getCode(), null));
                    }
                } else {
                    item.forEach((k, v) -> {
                        String kk = header.getOrDefault(k, String.valueOf(k));
                        nItem.put(kk, v);
                    });
                }

                nodeOutput.addResult(nItem);
                counter.incrementAndGet();
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                log.info("read complete, sheet: {}", analysisContext.readSheetHolder().getSheetName());
            }
        });
        if(firstHeader){
            readerBuilder.headRowNumber(1);
        }else{
            readerBuilder.headRowNumber(0);
        }
        readerBuilder.charset(encoding);
        readerBuilder.doReadAll();
        log.info("output:{}",nodeOutput.log());
    }

    private void write(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = buildOutput(nodeExecution);
        List<List<String>> headers = new ArrayList<>();
        List<List<Object>> items = new ArrayList<>();
        for (ExecutionInput input : nodeExecution.getInputs()) {
            for (Map<String, Object> inputItem : input.getData()) {
                if (headers.isEmpty()) {
                    headers = toHeader(inputItem);
                }
                items.add(toRow(inputItem,headers));
            }//end for inputItem
        }//end input
        File excelFile = new File(filePath);
        ExcelWriterSheetBuilder sheetBuilder = EasyExcel.write(excelFile).charset(encoding)
                .excelType(fileFormat).sheet(1);
        if (firstHeader) {
            sheetBuilder.head(headers).needHead(firstHeader);
        }
        sheetBuilder.doWrite(items);
        ExecutionResource executionResource = ExecutionResource.buildResource(excelFile,false);
        nodeOutput.addResource(0,executionResource);
    }

    private List<Object> toRow(Map<String, Object> item, List<List<String>> headers) {
        List<Object> row = new ArrayList<>();
        for (List<String> header : headers) {
            row.add(item.get(header.get(0)));
        }
        return row;
    }

    private List<List<String>> toHeader(Map<String, Object> item) {
        List<List<String>> headers = new ArrayList<>();
        if (selectColumn && selectedFields != null) {
            for (ParamField outputField : selectedFields) {
                headers.add(Lists.newArrayList(outputField.getCode()));
            }
        } else {
            for (String key : item.keySet()) {
                headers.add(Lists.newArrayList(key));
            }
        }
        return headers;
    }


    /**
     * operate operation
     */
    public enum WriteMode {

        /**
         * append data from file end
         */
        APPEND,

        /**
         * remove file and create new
         */
        OVERWRITE;
    }


    /**
     * excel file operation
     */
    public enum FileOperation {

        /**
         * opt
         */
        WRITE,
        READ;
    }


}
