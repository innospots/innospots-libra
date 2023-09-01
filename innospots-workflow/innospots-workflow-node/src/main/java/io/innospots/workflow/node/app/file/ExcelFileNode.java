package io.innospots.workflow.node.app.file;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/1
 */
public class ExcelFileNode extends BaseAppNode {

    private static final String FILE_OPT = "operation";

    private static final String FILE_PATH = "file_path";

    private static final String FILE_FORMAT = "file_format";
    private static final String LINE_SEPARATOR = "separator";
    private static final String HAS_FIRST_HEADER = "first_header";

    private static final String MAX_READ_LINE = "max_read_line";

    private static final String IS_SELECT_COLUMN = "is_select_column";

    private static final String OUTPUT_FIELDS = "output_fields";

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
    private FileFormat fileFormat;

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

    /**
     * output fields when write to excel
     */
    private List<ParamField> outputFields;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        this.fileOperation = FileOperation.valueOf(this.valueString(FILE_OPT));
        this.filePath = this.valueString(FILE_PATH);
        this.fileFormat = FileFormat.valueOf(this.valueString(FILE_FORMAT));
        if(fileFormat == FileFormat.csv){
            this.lineSeparator = this.valueString(LINE_SEPARATOR);
        }
        maxReadLines = nodeInstance.valueInteger(MAX_READ_LINE);
        firstHeader = nodeInstance.valueBoolean(HAS_FIRST_HEADER);
        List<Map<String, Object>> v = (List<Map<String, Object>>) nodeInstance.value(OUTPUT_FIELDS);
        if(v!=null){
            outputFields = BeanUtils.toBean(v,ParamField.class);
        }
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        switch(fileOperation){
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


    private void read(NodeExecution nodeExecution){
        NodeOutput nodeOutput = buildOutput(nodeExecution);
    }

    private void write(NodeExecution nodeExecution){
        NodeOutput nodeOutput = buildOutput(nodeExecution);
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

    public enum FileFormat {

        /**
         *
         */
        xls,
        xlsx,
        csv;
    }

}
