package org.smithe65.jlox;

import java.util.List;

public abstract class Statement {
  public interface Visitor<R> {
    R visitBlockStatement(Block statement);
    R visitExpressionStatement(Expression statement);
    R visitFunctionStatement(Function statement);
    R visitIfStatement(If statement);
    R visitPrintStatement(Print statement);
    R visitReturnStatement(Return statement);
    R visitVarStatement(Var statement);
    R visitWhileStatement(While statement);
  }
  public static class Block extends Statement {
    Block(List<Statement> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStatement(this);
    }

    final List<Statement> statements;
  }
  public static class Expression extends Statement {
    Expression(org.smithe65.jlox.Expression expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStatement(this);
    }

    final org.smithe65.jlox.Expression expression;
  }
  public static class Function extends Statement {
    Function(Token name, List<Token> parameters, List<Statement> body) {
      this.name = name;
      this.parameters = parameters;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStatement(this);
    }

    final Token name;
    final List<Token> parameters;
    final List<Statement> body;
  }
  public static class If extends Statement {
    If(org.smithe65.jlox.Expression condition, Statement thenBranch, Statement elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStatement(this);
    }

    final org.smithe65.jlox.Expression condition;
    final Statement thenBranch;
    final Statement elseBranch;
  }
  public static class Print extends Statement {
    Print(org.smithe65.jlox.Expression expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStatement(this);
    }

    final org.smithe65.jlox.Expression expression;
  }
  public static class Return extends Statement {
    Return(Token keyword, org.smithe65.jlox.Expression expression) {
      this.keyword = keyword;
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStatement(this);
    }

    final Token keyword;
    final org.smithe65.jlox.Expression expression;
  }
  public static class Var extends Statement {
    Var(Token name, org.smithe65.jlox.Expression initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStatement(this);
    }

    final Token name;
    final org.smithe65.jlox.Expression initializer;
  }
  public static class While extends Statement {
    While(org.smithe65.jlox.Expression condition, Statement body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStatement(this);
    }

    final org.smithe65.jlox.Expression condition;
    final Statement body;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
