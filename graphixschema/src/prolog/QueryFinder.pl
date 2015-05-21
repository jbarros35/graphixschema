% list of all fks
get_fks(TabX, List) :- findall(Z, is_fk(TabX, Z, _, _), List).

% list of all simple columns
get_cols(TabX, List) :- findall(Z, is_column(TabX, Z, _), List).

get_parents(Tab,Tparent) :- tree(Tparent,X), member(Tab,X).
% recursive function to find PK or the col value.
col_value(TabChild,Col,V) :- (is_fk(TabChild,Col,TabF,ColF)) ->
col_value(TabF,ColF,V); is_column(TabChild,Col,V),!.

high_lvl(X) :- is_table(X), not(is_fk(X,_,_,_)).

% check if a tableX is from directly blood line of a tableY.
bloodline(TabX,TabY) :- (tree(TabY,X), member(TabX,X)) -> true,!;
tree(TabY,X1),bloodline(TabX,TabY,X1).
% try recursively over tree dependencies
bloodline(TabX,TabY,[H|T]) :- (tree(TabY,X), member(TabX,X), X \= []) -> true,!;
bloodline(TabX,H,T).
% final predicate test the last dependency.
bloodline(TabX,TabY,[]) :- (tree(TabY,X), X \= []) -> bloodline(TabX,TabY,X); fail.
% get_pks(TabX, List) :- pk(TabX, X), findall(Z, is_fk(TabX, Z, _, _),
% FKList).
