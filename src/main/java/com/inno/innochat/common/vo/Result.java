package com.inno.innochat.common.vo;

import java.io.Serializable;

import org.apache.logging.log4j.util.Strings;

import lombok.Data;

/**
 * RESTFUL return type
 */
@Data
public class Result<T> implements Serializable {

    public static final int SUCCESS_CODE = 0;

	private static final long serialVersionUID = 1L;
	private String msg = Strings.EMPTY;
    private int code;
    private T data;
    public Result() {
        super();
    }
}

