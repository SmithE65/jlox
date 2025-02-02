package org.smithe65.jlox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.smithe65.jlox.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Statement declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Statement.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Statement.Class(name, methods);
    }

    private Statement.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name");
        consume(LEFT_PARENTHESIS, "Expect '(' after " + kind + " name");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PARENTHESIS)) {
            do {
                if (parameters.size() > 255) {
                    //noinspection ThrowableNotThrown
                    error(peek(), "Cannot have more than 255 parameters");
                }

                parameters.add(consume(IDENTIFIER, "Expect parameter name"));
            } while (match(COMMA));
        }

        consume(RIGHT_PARENTHESIS, "Expect ')' after parameter list");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body");
        List<Statement> body = block();
        return new Statement.Function(name, parameters, body);
    }

    private Statement varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected a variable name.");

        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }

    private Statement whileStatement() {
        consume(LEFT_PARENTHESIS, "Expected '(' after 'while'.");
        Expression condition = expression();
        consume(RIGHT_PARENTHESIS, "Expected ')' after 'while'.");
        Statement body = statement();

        return new Statement.While(condition, body);
    }

    private Statement forStatement() {
        consume(LEFT_PARENTHESIS, "Expected '(' after 'for'.");

        Statement initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!check(RIGHT_PARENTHESIS)) {
            increment = expression();
        }
        consume(RIGHT_PARENTHESIS, "Expected ')' after for clauses.");

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(
                    Arrays.asList(
                            body,
                            new Statement.Expression(increment)
                    )
            );
        }

        if (condition == null) {
            condition = new Expression.Literal(true);
        }

        body = new Statement.While(condition, body);

        if (initializer != null) {
            body = new Statement.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Statement statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private Statement ifStatement() {
        consume(LEFT_PARENTHESIS, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PARENTHESIS, "Expect ')' after 'if'.");
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expression(expression);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable)expression).name;
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Get get) {
                return new Expression.Set(get.object, get.name, value);
            }

            //noinspection ThrowableNotThrown
            error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return call();
    }

    private Expression call() {
        Expression expression = primary();

        while (true) {
            if (match(LEFT_PARENTHESIS)) {
                expression = finishCall(expression);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                expression = new Expression.Get(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();

        if (!check(RIGHT_PARENTHESIS)) {
            do {
                if (arguments.size() > 255) {
                    //noinspection ThrowableNotThrown
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PARENTHESIS, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(NIL)) return new Expression.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(THIS)) {
            return new Expression.This(previous());
        }

        if (match(IDENTIFIER)) {
            return new Expression.Variable(previous());
        }

        if (match(LEFT_PARENTHESIS)) {
            Expression expression = expression();
            consume(RIGHT_PARENTHESIS, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
