package cn.xiaym.modupdater.data.link;

public record CurseForgeLink(String projectId) implements ModLink {
    @Override
    public String toString() {
        return "CurseForge/" + projectId;
    }
}
