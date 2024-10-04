package io.innospots.workflow.core.response;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/4
 */
@Getter
@Setter
@Builder
public class RespPayload {

    private String icon;

    private String label;

    private int column;

    private CardAlign align;

    private CardLayout layout;

    private List<ViewCard> rows;

    public void addRow(ViewCard row) {
        if(rows == null){
            rows = Lists.newArrayList();
        }
        rows.add(row);
    }

}
