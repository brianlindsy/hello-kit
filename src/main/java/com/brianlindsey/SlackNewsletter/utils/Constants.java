package com.brianlindsey.SlackNewsletter.utils;

import java.util.ArrayList;

public class Constants {

	public static class Questions {
		public static String workHours = "What are your preferred work hours?";
		public static String timeZone = "What is your time zone?";
		public static String comm = "What is your preferred form of communication (slack, email, etc..)?";

		public static String pronouns = "What are your preferred pronouns (ex. he/him)?";
		public static String prevEmployer = "Who was your previous employer?";

		public static String vacation = "What was your last vacation and why?";
		public static String book = "What is your favorite book?";
		public static String tv = "What is your favorite TV show?";
		
		public static ArrayList<String> workHabitQuestions = new ArrayList<String>() {
			{
			add(workHours);
			add(timeZone);
			add(comm);
			}
		};
		public static ArrayList<String> personalQuestions = new ArrayList<String>() {
			{
			add(pronouns);
			add(prevEmployer);
			}
		};
		public static ArrayList<String> funQuestions = new ArrayList<String>() {
			{
			add(vacation);
			add(book);
			add(tv);
			}
		};
	}
}
