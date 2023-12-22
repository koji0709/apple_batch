package com.sgswit.fx.utils;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 样式工具类
 *
 * @author huhailong
 *
 */
public class StyleUtil {
	public static boolean first=true;
	public static void setPaneBackground(Pane pane, Color color) {
		pane.setBackground(new Background(new BackgroundFill(color, null, null)));
	}

	public static void setButtonBackground(Button button, Color bg, Color text) {
		button.setBackground(new Background(new BackgroundFill(bg, null, null)));
		button.setTextFill(text);
		button.setCursor(Cursor.HAND);
		Integer buttonIndex= (Integer) button.getUserData();
		BorderStroke borderStroke;
		if(buttonIndex==0){
			borderStroke = new BorderStroke(Color.BLACK, null, null, null, BorderStrokeStyle.SOLID, null, null,
					null, null, null, null);
		}else{
			borderStroke = new BorderStroke(null, null, Color.BLACK, null, null, null, BorderStrokeStyle.SOLID,
					null, null, null, null);
		}
		button.setBorder(new Border(borderStroke));
		button.setPadding(new Insets(10));
	}

	public static void setFont(Labeled node, Color color, double fontSize) {
		node.setTextFill(color);
		node.setFont(Font.font(null, FontWeight.BOLD, fontSize));
	}
}
