package de.relluem94.maven.plugin.itemcheckprice;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.bukkit.Material;

@Mojo(name = "checkItemPrice", threadSafe = true)
public class CheckItemPriceMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            URL[] urls = project.getCompileClasspathElements().stream()
                .map(path -> {
                    try {
                        return new java.io.File(path).toURI().toURL();
                    } catch (java.net.MalformedURLException e) {
                        getLog().error("Invalid path in project classpath: " + path, e);
                        return null;
                    }
                })
                .filter(url -> url != null)
                .toArray(URL[]::new);

            ClassLoader projectClassLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

            Class<?> enumClass = Class.forName(
                "de.relluem94.minecraft.server.spigot.essentials.constants.ItemPrice",
                true,
                projectClassLoader
            );

            if (!enumClass.isEnum()) {
                throw new MojoExecutionException("ItemPrice is not an enum!");
            }

            Object[] enumConstants = enumClass.getEnumConstants();
            Set<String> enumNames = new HashSet<>();
            for (Object constant : enumConstants) {
                enumNames.add(constant.toString());
            }

            for (Material type : Material.values()) {
                if (!enumNames.contains(type.name())) {
                    getLog().warn("Material missing in enum: " + type.name() + "(0,0),");
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("ItemPrice class not found in project", e);
        }
    }
}

