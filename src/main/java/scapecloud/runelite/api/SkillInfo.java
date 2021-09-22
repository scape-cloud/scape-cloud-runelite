package scapecloud.runelite.api;

import lombok.Data;

@Data
public class SkillInfo {
    private final String name;
    private final int boostedLevel;
    private final int currentLevel;
    private final int experience;
}
