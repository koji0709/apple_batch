package com.sgswit.fx.model;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Paint;


public class ColorTableCell extends TableCell {

    private String paint;

    public ColorTableCell(String paint){
        this.paint = paint;
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
            setTextFill(Paint.valueOf(paint));
        }
    }

}
