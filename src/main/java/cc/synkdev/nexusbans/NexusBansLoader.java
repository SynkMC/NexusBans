package cc.synkdev.nexusbans;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class NexusBansLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addDependency(new Dependency(new DefaultArtifact("mysql:mysql-connector-java:8.0.33"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mongodb:mongodb-driver-sync:5.13.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.github.classgraph:classgraph:4.8.184"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.json:json:20260814"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.xerial:sqlite-jdbc:3.53.4.0"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
