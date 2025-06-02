package org.example.appsmallcrm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Message {
    @Id
    private Long id;
    private String chatId;
    private String sender;
    private String text;
    private String time;
}