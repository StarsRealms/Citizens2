package net.citizensnpcs;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class LibLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        RemoteRepository central = new RemoteRepository.Builder(
                "central",
                "default",
                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build();
        addDependency(classpathBuilder, "ch.ethz.globis.phtree:phtree:2.8.2", central);
        addDependency(classpathBuilder, "org.joml:joml:1.10.8", central);
        addDependency(classpathBuilder, "it.unimi.dsi:fastutil:8.5.15", central);
    }

    public void addDependency(PluginClasspathBuilder classpathBuilder, String artifact, RemoteRepository... repositories) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        Dependency provided = new Dependency(new DefaultArtifact(artifact), "provided");
        resolver.addDependency(provided);
        for (RemoteRepository repository : repositories) {
            if (repository.getId().equals("central")) {
                addMavenCenterRepository(resolver);
            } else {
                resolver.addRepository(repository);
            }
        }
        classpathBuilder.addLibrary(resolver);
    }

    @SuppressWarnings("unchecked")
    public void addMavenCenterRepository(MavenLibraryResolver resolver) {
        RemoteRepository center = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
        try {
            Field repositories = MavenLibraryResolver.class.getDeclaredField("repositories");
            repositories.setAccessible(true);
            List<RemoteRepository> remoteRepositoryList = (List<RemoteRepository>) repositories.get(resolver);
            remoteRepositoryList.add(center);
        } catch (Exception ignored) {
        }
    }
}
