Semantic checks implemented:

• <id> names (arrays and variables) must be declared before they are used;
• array size – must be known at compile time;
• strong typing exists for real variables, real arrays, boolean expressions, and arithmetic operations (such as numeric ^ INTEGER);
• valid assignment operations;
• actual parameters in a procedure or function call must match the type of their respective formal parameter in the procedure definition;
• the number of actual parameters in a procedure call must be equal to the number of formal parameters in the procedure definition;
• a function must have at least one return statement.
• <id> names must be unique at their particular block level (scoping)

Semantic checks not implemented:

• Constant array type semantic checks using the node type NARRC.
• A function cannot be called before it is formally defined in the source code.

-----------------------------------------------------------------------------------

Code generation aspects implemented:

• Functions
• Functions in functions
• Arrays
• Constants
• Repeats
• Fors
• Ifs
• If then elses
• Inputs
• Printlines
• Returns
• Assigns (+= = /= *=)
• Logical operations (and or xor)
• Relative operations (< > <= == >=)
• Expressions (+ -)
• Terms (* / %)
• Facts (^)
• Brackets (())

Code generation aspects partially implemented:

• Array assign statements with whole Arrays (eg. Array1 = Array2) are only applied for the current scope see report for details

-----------------------------------------------------------------------------------

See my test file Polygon.cd in the Polygon folder for a quick demo