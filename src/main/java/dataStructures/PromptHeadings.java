package dataStructures;

import java.util.ArrayList;

public class PromptHeadings {
	private static ArrayList<String> headings = new ArrayList<String>();
	// This should only ever be called on Zyenyo startup, or on urcprompts. therefore it is assumed here that the heading array index and the prompt index will match up. Using this in other places may cause problems
	public static void addHeading(String heading) {
		headings.add(heading);
	}

	public static String get(Integer index) {
		return headings.get(index);
	}
}
