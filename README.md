Crux Symbol Table
====

###Introduction

A compiler is comprised of seven stages:
-	Lexical analysis: Identify the logical pieces of the input.
-	Syntax analysis: Identify how those pieces relate to each other.
-	Semantic analysis: Identify the meaning of the overall structure.
-	IR Generation: Design one possible structure.
-	IR Optimization: Simplify the intended structure.
-	Generation: Fabricate the structure.
-	Optimization: Improve the resulting structure.

###Project

The last project created a parser that can determine if the source input is a valid Crux program by attempting to create a parse tree. If the parse tree can successfully be created, then the source input follows the Crux grammar.

This time the parser will be modified to include a symbol table to allow declaration and 'resolvation' of identifiers. In other words, the parser will now be abled to recognize scopes: both global and local.

###Classes

Compiler: Takes a source input file and tries to parse the source input. If it parses succesfully, a "Crux program successfully parsed." will be generated for the user. If not, an error message will be generated instead.

Scanner: Uses a greedy approach to generate the tokens from a source input.

Parser: Uses the scanner to generate the tokens from the source input and creates a parse tree from them.

Token: Represents the logical pieces of Crux.

NonTerminal: Contains the First Set of each production rule.

Symbol: Represents an identifier.

Symbol Table: Represents scopes.

####Sample

#####Input

```
var gorn : void;
func main(a:void) : int {
  var b:float;
  let a = 1;
}

var kirk : boolean;

func destroy_all_humans(lambda: void, laMbda: int) : void
{
    var greek : int;
    while (not 4) {
        return 88 * ::main();
        var x : int;
        let _ = 8 and 9;
    }
    let x = 9;
    let y = 20;
}

var gorn : void;
```

#####Output
```
Error parsing file.
ResolveSymbolError(15,13)[Could not find _.]
Symbol(readInt)
Symbol(readFloat)
Symbol(printBool)
Symbol(printInt)
Symbol(printFloat)
Symbol(println)
Symbol(gorn)
Symbol(main)
Symbol(kirk)
Symbol(destroy_all_humans)
  Symbol(lambda)
  Symbol(laMbda)
  Symbol(greek)
    Symbol(x)

ResolveSymbolError(17,9)[Could not find x.]
Symbol(readInt)
Symbol(readFloat)
Symbol(printBool)
Symbol(printInt)
Symbol(printFloat)
Symbol(println)
Symbol(gorn)
Symbol(main)
Symbol(kirk)
Symbol(destroy_all_humans)
  Symbol(lambda)
  Symbol(laMbda)
  Symbol(greek)

ResolveSymbolError(18,9)[Could not find y.]
Symbol(readInt)
Symbol(readFloat)
Symbol(printBool)
Symbol(printInt)
Symbol(printFloat)
Symbol(println)
Symbol(gorn)
Symbol(main)
Symbol(kirk)
Symbol(destroy_all_humans)
  Symbol(lambda)
  Symbol(laMbda)
  Symbol(greek)

DeclareSymbolError(21,5)[gorn already exists.]
Symbol(readInt)
Symbol(readFloat)
Symbol(printBool)
Symbol(printInt)
Symbol(printFloat)
Symbol(println)
Symbol(gorn)
Symbol(main)
Symbol(kirk)
Symbol(destroy_all_humans)
```

Previous: https://github.com/AFM-TN/crux-parser

Next: TBA
