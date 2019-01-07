package protocolsupportbuildprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
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
			File file = new File(processingEnv.getOptions().get(preloadFilePathOptionName));
			file.getParentFile().mkdirs();
			Files.write(
				file.toPath(),
				env.getElementsAnnotatedWith(Preload.class).stream()
				.map(element -> ((TypeElement) element).getQualifiedName())
				.collect(Collectors.toList()),
				StandardOpenOption.CREATE
			);
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Unable to write generated preload list: " + e.getMessage());
		}
		return true;
	}

}
