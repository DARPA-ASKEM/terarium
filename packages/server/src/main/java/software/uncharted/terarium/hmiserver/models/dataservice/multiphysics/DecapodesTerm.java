package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

//////
// Note: Decapode term can be one of many different types. As in
// term = A or B or C or ... wher A, B, C, D ... are different objects.
//
// Since Java does not have type-disjunctions this is represented as a
// union of all the known types. See below
//
// October 2023:
//   Var(name::Symbol)
//   Lit(name::Symbol)
//   Judgement(var::Var, dim::Symbol, space::Symbol)
//   AppCirc1(fs::Vector{Symbol}, arg::Term)
//   App1(f::Symbol, arg::Term)
//   App2(f::Symbol, arg1::Term, arg2::Term)
//   Plus(args::Vector{Term})
//   Mult(args::Vector{Term})
//   Tan(var::Term)
//////

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesTerm {
	@TSOptional
	private String name;

	@TSOptional
	private DecapodesTerm var;

	@TSOptional
	private String symbol;

	@TSOptional
	private String space;

	@TSOptional
	private List<String> fs;

	@TSOptional
	private DecapodesTerm arg;

	@TSOptional
	private String f;

	@TSOptional
	private DecapodesTerm arg1;

	@TSOptional
	private DecapodesTerm arg2;

	@TSOptional
	private List<DecapodesTerm> args;

	private String _type;
}
