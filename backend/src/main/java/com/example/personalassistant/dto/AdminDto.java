package com.example.personalassistant.dto;

import com.example.personalassistant.enums.AccountEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDto {

    private Long hotelId;
    private Long id;
    private String email;
    private String name;
    private Long number;
    private String password;
    private String suspendEmailId;
    private String SuspendId;
    private String hotel;
    private AccountEnum accountEnum;
    private int availableRooms;
}
