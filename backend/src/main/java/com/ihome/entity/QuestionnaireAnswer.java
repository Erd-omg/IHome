package com.ihome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("questionnaire_answers")
public class QuestionnaireAnswer {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String studentId;
    private String sleepTimePreference;
    private String cleanlinessLevel;
    private String noiseTolerance;
    private String eatingInRoom;
    private String collectiveSpendingHabit;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getSleepTimePreference() { return sleepTimePreference; }
    public void setSleepTimePreference(String sleepTimePreference) { this.sleepTimePreference = sleepTimePreference; }
    public String getCleanlinessLevel() { return cleanlinessLevel; }
    public void setCleanlinessLevel(String cleanlinessLevel) { this.cleanlinessLevel = cleanlinessLevel; }
    public String getNoiseTolerance() { return noiseTolerance; }
    public void setNoiseTolerance(String noiseTolerance) { this.noiseTolerance = noiseTolerance; }
    public String getEatingInRoom() { return eatingInRoom; }
    public void setEatingInRoom(String eatingInRoom) { this.eatingInRoom = eatingInRoom; }
    public String getCollectiveSpendingHabit() { return collectiveSpendingHabit; }
    public void setCollectiveSpendingHabit(String collectiveSpendingHabit) { this.collectiveSpendingHabit = collectiveSpendingHabit; }
}


