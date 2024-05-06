package prog2.fingrp;

import java.util.*;
import java.io.*;

/**
 * Development notes:
 * We split the record into 2 array lists so changes made by the user can be stored separately
 * from the template course files.
 * 1. Compiled record
 *  - Initially the data from the template file. This is what gets passed to the GUI when
 *      they ask for data from the CurriculumRecord.
 *  - Sorting and filtering functions should return a modified version of this array list.
 * 2. Personal record
 *  - These are the changes made by the user. Refer to the mergeData function of the Course class
 *      to see what values constitute as a change.
 *  - This is what gets saved to the personal record .dat file using the saveChanges() function.
 *
 */

public class CurriculumRecord {
   private ArrayList<Course> templateRecord; //Possible use..? Remove later if not
    private static ArrayList<Course> personalRecord;
    private ArrayList<Course> compiledRecord;

    public CurriculumRecord(InputStream templateRecord, InputStream personalRecord) throws IOException, ClassNotFoundException{
        this.templateRecord = (ArrayList<Course>) new ObjectInputStream(templateRecord).readObject();
        this.personalRecord = (ArrayList<Course>) new ObjectInputStream(personalRecord).readObject();

        //Early close the files. We won't be needing them anymore.
        templateRecord.close();
        personalRecord.close();
        compiledRecord = this.templateRecord;


        //Find the first match of the courses with changes and merge their data with the existing data.
        for (Course personalData: this.personalRecord){
            boolean matchFound = false;
            for(Course outputCourse: this.compiledRecord){
                if (outputCourse.equals(personalData)){
                    outputCourse.mergeData(personalData);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) compiledRecord.add(personalData);
        }
    }

    public CurriculumRecord(InputStream templateRecord) throws IOException, ClassNotFoundException{
        this.compiledRecord = (ArrayList<Course>) new ObjectInputStream(templateRecord).readObject();
        this.personalRecord = new ArrayList<Course>();

        templateRecord.close();
    }

    public void setFilter(ArrayList<Course> courses){
        personalRecord = courses;
    }

    public static ArrayList<Course> getCourseList() {
        return personalRecord;
    }

    //Saving file only outputs files.
    public void saveChanges(OutputStream out) throws IOException {
        ObjectOutputStream outputFile = new ObjectOutputStream(out);
        outputFile.writeObject(personalRecord);
        out.close();
    }

    public void editCourse(String courseCode, Course.CourseBuilder courseData) {
        //Edit or add to compiled list of courses
        boolean matchFound = false;
        for (Course outputCourse:compiledRecord){
            if (outputCourse.getCode().equals(courseCode)){
                outputCourse.mergeData(courseData);
                matchFound = true;
                break;
            }
        }

        //Course is not part of existing records
        if (!matchFound)
        {
            //Assume it is additional if first time being added after adding template record files.
            courseData.additionalStatus(true);
            compiledRecord.add(new Course(courseData));
            personalRecord.add(new Course(courseData));
            return;
        }

        //Check if course exists in already edited courses.
        matchFound = false;
        for(Course personalData: personalRecord){
            if (personalData.getCode().equals(courseCode)) {
                personalData.mergeData(courseData);
                matchFound = true;
                break;
            }
        }
        if (!matchFound) personalRecord.add(new Course(courseData));
    }
    public void deleteCourse(String courseCode) {
        for (Course outputCourse : personalRecord) {
            if (outputCourse.getCode().equals(courseCode)) {
                personalRecord.remove(outputCourse);
                break;
            }
        }

    }
    //INCOMPLETE
    public ArrayList<Course> FilterByYearAndTerm(int year, int term) {
        List<Course> filteredCourses = CurriculumRecord.getCourseList().stream()
                .filter(course -> course.getYear() == year && course.getTerm() == term).collect(Collectors.toList());
        return (ArrayList<Course>) filteredCourses;
    }

    public ArrayList<Course> FilterByCurriculum(boolean isAdditional){
        List<Course> filteredCourses = CurriculumRecord.getCourseList().stream()
                .filter(e -> e.isAdditional() == isAdditional).collect(Collectors.toList());
        return (ArrayList<Course>) filteredCourses;
    }

    public ArrayList<Course> SortByGrade(boolean descending){
        List<Course> filteredCourses;
        if(descending){
            filteredCourses = CurriculumRecord.getCourseList().stream().filter(e -> e.getGrade() > 0).sorted((o1, o2) -> {
                //If result is negative, returns o1.
                //If result is positive returns o2.
                return (int) (o2.getGrade() - o1.getGrade());
            }).collect(Collectors.toList());
        }else {
            filteredCourses = CurriculumRecord.getCourseList().stream().filter(e -> e.getGrade() > 0).sorted((o1, o2) -> {
                return (int) (o1.getGrade() - o2.getGrade());
            }).collect(Collectors.toList());
        }
        // Return the filtered list
        return (ArrayList<Course>) filteredCourses;
    }

//WORK IN PROGRESS
    public ArrayList<Course> SortByGPA(boolean descending){ return compiledRecord;
    }

    public ArrayList<Course> SortByTitle(boolean descending){
        List<Course> filteredCourses;
        if(!descending){
            filteredCourses = CurriculumRecord.getCourseList().stream().filter(e -> e.getTitle() != null)
                    .sorted(Comparator.comparing(Course::getTitle))
                    .collect(Collectors.toList());
        }else{
            filteredCourses = CurriculumRecord.getCourseList().stream().filter(e -> e.getTitle() != null)
                    .sorted((o1, o2) -> o2.getTitle().compareTo(o1.getTitle()))
                    .collect(Collectors.toList());
        }

        // Return the filtered list
        return (ArrayList<Course>) filteredCourses;
    }
}


    public ArrayList<Course> SortByTitle(boolean descending){
        return compiledRecord;
    }
}
