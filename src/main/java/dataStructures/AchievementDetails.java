package dataStructures;

/**
 * Data about a specific achievement.
 * @param title : name of achievement
 * @param description : Description of the achievment
 * @param thumbnail : the achievement icon, that will be displayed in the embed.
 */
public record AchievementDetails(String title, String description, String thumbnail ) {}
