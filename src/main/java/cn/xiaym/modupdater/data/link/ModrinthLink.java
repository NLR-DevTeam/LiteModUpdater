package cn.xiaym.modupdater.data.link;

public record ModrinthLink(String projectId) implements ModLink {
    @Override
    public String toString() {
        return "Modrinth/" + projectId;
    }
}
