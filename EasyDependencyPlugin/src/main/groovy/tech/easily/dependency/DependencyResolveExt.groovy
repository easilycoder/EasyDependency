package tech.easily.dependency

class DependencyResolveExt {

    final String name

    DependencyResolveExt(String name) {
        this.name = name
    }
/**
     * if you need to depend on the lib as a module dependency ,set to true
     *
     * otherwise,the lib will be depended as an aar dependency using the config as below like:
     *
     * compile groupId:artifactId:version
     */
    boolean debuggable=true
    /**
     * the aar dependency config part of groupId
     */
    String groupId
    /**
     * the aar dependency config part of artifactId
     */
    String artifactId
    /**
     * the aar dependency config part of version
     */
    String version

}