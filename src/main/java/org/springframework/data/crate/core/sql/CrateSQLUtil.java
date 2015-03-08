package org.springframework.data.crate.core.sql;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.split;
import static org.springframework.util.StringUtils.collectionToDelimitedString;
import static org.springframework.util.StringUtils.hasText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrateSQLUtil {
	
	private static final String DOUBLE_QUOTE_TEMPLATE = "\"%s\"";
	private static final String SINGLE_QUOTE_TEMPLATE = "'%s'";
	private static final String SQL_PATH_TEMPLATE = "['%s']";
	
	private static final Pattern PATTERN = compile("\\['([^\\]]*)'\\]");
    private static final Pattern CRATE_SQL_PATTERN = compile("(.+?)(?:\\['([^\\]])*'\\])+");
	
	public static String doubleQuote(String toQuote) {
		
		if(hasText(toQuote)) {
			return format(DOUBLE_QUOTE_TEMPLATE, toQuote);
		}
		
		return toQuote;
	}
	
	public static String singleQuote(String toQuote) {
		
		if(hasText(toQuote)) {
			return format(SINGLE_QUOTE_TEMPLATE, toQuote);
		}
		
		return toQuote;
	}
	
	public static String dotToSqlPath(String dotPath) {
		
		if(hasText(dotPath)) {
			
			String[] tokens = split(dotPath, ".");
			
			// double quotes to preserve case in crate db
			StringBuilder sqlPath = new StringBuilder(doubleQuote(tokens[0]));
			
			for (int i = 1; i < tokens.length; i++) {
				sqlPath.append(format(SQL_PATH_TEMPLATE, tokens[i]));
			}
			
			return sqlPath.toString();
		}
		
		return dotPath;
	}
	
	public static String sqlToDotPath(String sqlPath) {
		
		if (!isQualifiedExpression(sqlPath)) {
        	return sqlPath;
        }

        int index = sqlPath.indexOf('[');
        
        List<String> tokens = new ArrayList<String>();
        tokens.add(sqlPath.substring(0, index));
        
        Matcher matcher = PATTERN.matcher(sqlPath);
        while (matcher.find(index)) {
            String group = matcher.group(1);
            if (group == null) {
            	group = "";
            }
            tokens.add(group);            
            index = matcher.end();
        }
        
        return collectionToDelimitedString(tokens, ".");
	}
	
	public static boolean isQualifiedExpression(String sqlPath) {
		return CRATE_SQL_PATTERN.matcher(sqlPath).find();
	}
}