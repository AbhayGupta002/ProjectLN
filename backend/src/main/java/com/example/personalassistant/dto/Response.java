package com.example.personalassistant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    Object data;
    ErrorDetails error;
}
