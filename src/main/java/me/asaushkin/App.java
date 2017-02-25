package me.asaushkin;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class App {
	
	@Option(name = "-f", aliases = "--file", usage = "Full path to file with words", required = true) 
	File parseFile;
	
	@Option(name = "-r", aliases = "--remove-chars", usage = "A regex for chars what you need to remove")
	String removeRegex = "[-!:\\?,\\.'\"]";
	
	@Option(name = "-s", aliases = "--split-words-by", usage = "Split words with this regular expression")
	String splitBy = "[\\s]+";
	
	@Option(name = "-M", aliases = "--max-chars-limit", usage = "Exclude words more then N char length (inclusive)")
	Integer maxChars = 3;
	
	@Option(name = "-m", aliases = "--min-usage-limit", usage = "Exclude words with count in the text less then N (inclusive)")
	Integer maxUsageInText = 3;
	
	@Option(name = "-h", aliases = {"-?", "--help"}, usage = "Show this help", help = true)
	Boolean help = false;

	public static void main(String[] args) throws CmdLineException, IOException {
		
		final App app = new App();
		final CmdLineParser cmdLineParser = new CmdLineParser(app);
		
		cmdLineParser.parseArgument(args);

		if (app.help) {
			cmdLineParser.printUsage(System.out);
			return;
		}
		
		Files.lines(app.parseFile.toPath())
				.map(String::toLowerCase)
				.map(line -> line.replaceAll(app.removeRegex, ""))
				.map(line -> line.split(app.splitBy))
				.flatMap(Arrays::stream)
				.collect(groupingBy(Function.identity(), HashMap::new, counting()))
				.entrySet()
				.stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.filter(w -> w.getKey().length() > app.maxChars)
				.filter(w -> w.getValue() > app.maxUsageInText)
				.forEachOrdered(x -> System.out.println(x.getKey()));
	}
}
