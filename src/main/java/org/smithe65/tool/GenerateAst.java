package org.smithe65.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expression", Arrays.asList(
                "Assign   : Token name, Expression value",
                "Binary   : Expression left, Token operator, Expression right",
                "Call     : Expression callee, Token paren, List<Expression> arguments",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Logical  : Expression left, Token operator, Expression right",
                "Unary    : Token operator, Expression right",
                "Variable : Token name"
        ));

        defineAst(outputDir, "Statement", Arrays.asList(
                "Block      : List<Statement> statements",
                "Expression : org.smithe65.jlox.Expression expression",
                "Function   : Token name, List<Token> parameters, List<Statement> body",
                "If         : org.smithe65.jlox.Expression condition, Statement thenBranch, Statement elseBranch",
                "Print      : org.smithe65.jlox.Expression expression",
                "Return     : Token keyword, org.smithe65.jlox.Expression expression",
                "Var        : Token name, org.smithe65.jlox.Expression initializer",
                "While      : org.smithe65.jlox.Expression condition, Statement body"
        ));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package org.smithe65.jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        System.out.println("*** Writing class " + className);
        writer.println("  public static class " + className + " extends " +
                baseName + " {");

        // Constructor.
        writer.println("    " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        System.out.println("Fields: " + fieldList);
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            System.out.println(field);
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {

        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
}
