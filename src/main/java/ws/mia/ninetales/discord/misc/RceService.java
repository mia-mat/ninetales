package ws.mia.ninetales.discord.misc;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.mongo.MongoUserService;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class RceService implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	public Object execute(String source, List<String> imports) {
		UUID requestId = UUID.randomUUID();

		try {
			Path tempDirPath = Files.createTempDirectory("remote");

			String className = "r" + requestId.toString().replaceAll("-", "");

			StringBuilder sbFormattedImports = new StringBuilder();
			for (String i : imports) {
				sbFormattedImports.append("import ").append(i).append(";").append("\n");
			}

			String fullSource = """
					import java.util.*; // for UUID
					import org.springframework.context.ApplicationContext; // enable bean access
					
					// just a few of these
					import ws.mia.ninetales.discord.misc.*;
					import ws.mia.ninetales.hypixel.*;
					import ws.mia.ninetales.mongo.*;
					
					// user defined imports
					%s
					
					public class %s {
						public static Object run(ApplicationContext context) {
							%s
						}
					
					}
					""".formatted(sbFormattedImports.toString(),
					className, source);

			// save source in .java file.
			File tempDirPathFile = new File(tempDirPath.toString());
			File sourceFile = new File(tempDirPathFile, className + ".java");
			Writer writer = new FileWriter(sourceFile);
			writer.write(fullSource);
			writer.close();

			JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
			ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

			// attempt to compile
			String classpath = System.getProperty("java.class.path");
			javac.run(null, null, errorStream, "--release", "17", "-cp", classpath, sourceFile.getPath());

			if (!errorStream.toString().isBlank()) {
				String ret = errorStream.toString();
				errorStream.close();
				return ret;
			}

			URLClassLoader classLoader = new URLClassLoader(
					new URL[]{tempDirPathFile.toURI().toURL()},
					Thread.currentThread().getContextClassLoader()
			);

			Class<?> rceClass = classLoader.loadClass("%s".formatted(className));
			Object evalOutput = rceClass.getDeclaredMethod("run", ApplicationContext.class).invoke(null, applicationContext);
			classLoader.close();

			// delete temp dir after use
			for (File file : tempDirPathFile.listFiles()) {
				Files.delete(file.toPath());
			}
			Files.delete(tempDirPath);

			return evalOutput;
		} catch (Exception e) {
			return e;
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
