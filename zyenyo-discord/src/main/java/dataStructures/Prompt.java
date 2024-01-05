package dataStructures;

import java.util.Objects;

public record Prompt(int number, String title, String body, int length, double typeRating)
{
	public Prompt
	{
		Objects.requireNonNull(title);
		Objects.requireNonNull(body);
	}
}
