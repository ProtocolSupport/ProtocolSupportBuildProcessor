package protocolsupportbuildprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class AnnotationProcessor extends AbstractProcessor {

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return new HashSet<>(Arrays.asList(
			Preload.class.getName()
		));
	}

	protected static final String preloadFilePathOptionName = "protocolsupportbuildprocessor.generatedresourcesdirectory";

	@Override
	public Set<String> getSupportedOptions() {
		return new HashSet<>(Arrays.asList(preloadFilePathOptionName));
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		try {
			Path path = Paths.get(processingEnv.getOptions().get(preloadFilePathOptionName));
			if (Files.exists(path)) {
				//load existing entries to list
				Set<String> list = new LinkedHashSet<>(Files.readAllLines(path));
				//remove entries for classes that aren't annotated
				env.getRootElements().stream()
				.filter(element -> element.getAnnotation(Preload.class) == null)
				.map(element -> ((TypeElement) element).getQualifiedName())
				.forEach(annotatedName -> list.remove(annotatedName.toString()));
				//add entries for classes that are annotated
				env.getElementsAnnotatedWith(Preload.class).stream()
				.map(element -> ((TypeElement) element).getQualifiedName())
				.forEach(annotatedName -> list.add(annotatedName.toString()));
				//write entries
				Files.write(path, list, StandardOpenOption.TRUNCATE_EXISTING);
			} else {
				Files.createDirectories(path.getParent());
				Files.write(
					path,
					env.getElementsAnnotatedWith(Preload.class).stream()
					.map(element -> ((TypeElement) element).getQualifiedName())
					.collect(Collectors.toList()),
					StandardOpenOption.CREATE
				);
			}
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Unable to write generated preload list: " + e.getMessage());
		}
		return true;
	}

}
