package org.arend.lib.meta.equation;

import org.arend.ext.concrete.ConcreteFactory;
import org.arend.ext.concrete.expr.*;
import org.arend.ext.core.definition.*;
import org.arend.ext.core.expr.CoreExpression;
import org.arend.ext.core.ops.CMP;
import org.arend.ext.core.ops.NormalizationMode;
import org.arend.ext.dependency.Dependency;
import org.arend.ext.error.ErrorReporter;
import org.arend.ext.error.GeneralError;
import org.arend.ext.error.ListErrorReporter;
import org.arend.ext.error.TypecheckingError;
import org.arend.ext.typechecking.*;
import org.arend.lib.StdExtension;
import org.arend.lib.util.Maybe;
import org.arend.lib.error.TypeError;
import org.arend.lib.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EquationMeta extends BaseMetaDefinition {
  final StdExtension ext;

  @Dependency(module = "Algebra.Pointed")                          public CoreClassDefinition Pointed;
  @Dependency(module = "Algebra.Pointed")                          public CoreClassDefinition AddPointed;
  @Dependency(module = "Algebra.Pointed", name = "Pointed.ide")    public CoreClassField ide;
  @Dependency(module = "Algebra.Pointed", name = "AddPointed.zro") public CoreClassField zro;

  @Dependency(module = "Algebra.Monoid")                       CoreClassDefinition Monoid;
  @Dependency(module = "Algebra.Monoid")                       CoreClassDefinition CMonoid;
  @Dependency(module = "Algebra.Monoid")                       CoreClassDefinition AddMonoid;
  @Dependency(module = "Algebra.Monoid")                       CoreClassDefinition AbMonoid;
  @Dependency(module = "Algebra.Monoid", name = "Monoid.*")    CoreClassField mul;
  @Dependency(module = "Algebra.Monoid", name = "Monoid.LDiv") CoreClassDefinition ldiv;
  @Dependency(module = "Algebra.Monoid", name = "Monoid.RDiv") CoreClassDefinition rdiv;
  @Dependency(module = "Algebra.Monoid", name = "AddMonoid.+") public CoreClassField plus;
  @Dependency(module = "Algebra.Monoid", name = "AddMonoid.zro-right") public CoreClassField addMonZroRight;

  @Dependency(module = "Order.Lattice", name = "Bounded.MeetSemilattice")         CoreClassDefinition MSemilattice;
  @Dependency(module = "Order.Lattice", name = "MeetSemilattice.meet")            CoreClassField meet;
  @Dependency(module = "Order.Lattice", name = "Bounded.MeetSemilattice.top")     CoreClassField top;
  @Dependency(module = "Order.Lattice", name = "JoinSemilattice.join")            CoreClassField join;
  @Dependency(module = "Order.Lattice", name = "Bounded.JoinSemilattice.bottom")  CoreClassField bottom;
  @Dependency(module = "Order.Lattice", name = "Bounded.DistributiveLattice")     CoreClassDefinition BoundedDistributiveLattice;

  @Dependency(module = "Order.LinearOrder")                                           public CoreClassDefinition LinearOrder;
  @Dependency(module = "Order.StrictOrder", name = "StrictPoset.<")                   public CoreClassField less;
  @Dependency(module = "Order.LinearOrder", name = "BiorderedSet.<-transitive-left")  public CoreClassField lessTransitiveLeft;

  @Dependency(module = "Algebra.Monoid.Solver", name = "MonoidTerm.var")  CoreConstructor varMTerm;
  @Dependency(module = "Algebra.Monoid.Solver", name = "MonoidTerm.:ide") CoreConstructor ideMTerm;
  @Dependency(module = "Algebra.Monoid.Solver", name = "MonoidTerm.:*")   CoreConstructor mulMTerm;

  @Dependency(module = "Category", name = "Precat.o")      CoreClassField catMul;
  @Dependency(module = "Category", name = "Precat.Hom")      public CoreClassField catHom;

  @Dependency(module = "Category.Solver", name = "CatTerm.var")           CoreConstructor varCTerm;
  @Dependency(module = "Category.Solver", name = "CatTerm.:id")           CoreConstructor idCTerm;
  @Dependency(module = "Category.Solver", name = "CatTerm.:o")            CoreConstructor compCTerm;
  @Dependency(module = "Category.Solver", name = "CatNF.:nil")            CoreConstructor nilCatNF;
  @Dependency(module = "Category.Solver", name = "CatNF.:cons")           CoreConstructor consCatNF;
  @Dependency(module = "Category.Solver")                                 CoreClassDefinition HData;
  @Dependency(module = "Category.Solver", name = "HData.H")               CoreClassField HDataFunc;
  @Dependency(module = "Category.Solver", name = "HData.V")               CoreClassField VDataFunc;
  @Dependency(module = "Category.Solver", name = "HData.terms-equality")  CoreFunctionDefinition catTermsEq;
  @Dependency(module = "Category.Solver", name = "HData.normalize-consistent")     CoreFunctionDefinition catNormConsist;
  @Dependency(module = "Category.Solver", name = "HData.interpretNF")     CoreFunctionDefinition catInterpretNF;
  @Dependency(module = "Category.Solver", name = "HData.interpretNF_++")  CoreFunctionDefinition catInterpretNFConcat;

  @Dependency(module = "Algebra.Monoid.Solver")                                       CoreClassDefinition Data;
  @Dependency(module = "Algebra.Monoid.Solver")                                       CoreClassDefinition CData;
  @Dependency(module = "Algebra.Monoid.Solver")                                       CoreClassDefinition LData;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.f")                      CoreClassField DataFunction;
  @Dependency(module = "Algebra.Monoid.Solver", name = "LData.L")                     CoreClassField SemilatticeDataCarrier;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.terms-equality")         CoreFunctionDefinition termsEq;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.terms-equality-conv")    CoreFunctionDefinition termsEqConv;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.replace-consistent")     CoreFunctionDefinition replaceDef;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.normalize-consistent")   CoreFunctionDefinition monoidNormConsist;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.interpretNF")            CoreFunctionDefinition monoidInterpretNF;
  @Dependency(module = "Algebra.Monoid.Solver", name = "Data.interpretNF_++")         CoreFunctionDefinition monoidInterpretNFConcat;
  @Dependency(module = "Algebra.Monoid.Solver", name = "CData.terms-equality")        CoreFunctionDefinition commTermsEq;
  @Dependency(module = "Algebra.Monoid.Solver", name = "CData.terms-equality-conv")   CoreFunctionDefinition commTermsEqConv;
  @Dependency(module = "Algebra.Monoid.Solver", name = "CData.replace-consistent")    CoreFunctionDefinition commReplaceDef;
  @Dependency(module = "Algebra.Monoid.Solver", name = "CData.sort-consistent")       CoreFunctionDefinition sortDef;
  @Dependency(module = "Algebra.Monoid.Solver", name = "LData.terms-equality")        CoreFunctionDefinition semilatticeTermsEq;

  @Dependency(module = "Algebra.Ring.Solver")                                         CoreClassDefinition SemiringData;
  @Dependency(module = "Algebra.Ring.Solver")                                         CoreClassDefinition RingData;
  @Dependency(module = "Algebra.Ring.Solver")                                         CoreClassDefinition CSemiringData;
  @Dependency(module = "Algebra.Ring.Solver")                                         CoreClassDefinition CRingData;
  @Dependency(module = "Algebra.Ring.Solver", name = "SemiringData.R")                CoreClassField RingDataCarrier;
  @Dependency(module = "Algebra.Ring.Solver")                                         CoreClassDefinition LatticeData;
  @Dependency(module = "Algebra.Ring.Solver", name = "LatticeData.L")                 CoreClassField LatticeDataCarrier;

  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.var")              CoreConstructor varTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.coef")             CoreConstructor coefTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.:ide")             CoreConstructor ideTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.:zro")             CoreConstructor zroTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.:negative")        CoreConstructor negativeTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.:*")               CoreConstructor mulTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingTerm.:+")               CoreConstructor addTerm;
  @Dependency(module = "Algebra.Ring.Solver", name = "AlgData.terms-equality")    CoreFunctionDefinition semiringTermsEq;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingData.terms-equality")   CoreFunctionDefinition ringTermsEq;
  @Dependency(module = "Algebra.Ring.Solver", name = "CAlgData.terms-equality")   CoreFunctionDefinition commSemiringTermsEq;
  @Dependency(module = "Algebra.Ring.Solver", name = "CRingData.terms-equality")  CoreFunctionDefinition commRingTermsEq;
  @Dependency(module = "Algebra.Ring.Solver", name = "LatticeData.terms-equality")  CoreFunctionDefinition latticeTermsEq;
  @Dependency(module = "Algebra.Ring.Solver", name = "RingData.interpret")        CoreFunctionDefinition ringInterpret;
  @Dependency(module = "Algebra.Ring.Solver", name = "gensZeroToIdealZero")       CoreFunctionDefinition gensZeroToIdealZero;

  @Dependency(module = "Algebra.Group", name = "AddGroup.negative")       public CoreClassField negative;
  @Dependency(module = "Algebra.Group", name = "AddGroup.fromZero")       public CoreFunctionDefinition fromZero;
  @Dependency(module = "Algebra.Group", name = "AddGroup.toZero")         public CoreFunctionDefinition toZero;
  @Dependency(module = "Algebra.Semiring")                                public CoreClassDefinition Semiring;
  @Dependency(module = "Algebra.Ring")                                    public CoreClassDefinition Ring;
  @Dependency(module = "Algebra.Semiring", name = "Semiring.natCoef")     public CoreClassField natCoef;
  @Dependency(module = "Algebra.Semiring", name = "Semiring.zro_*-right") public CoreClassField zeroMulRight;
  @Dependency(module = "Algebra.Ring", name = "Ring.intCoef")             public CoreFunctionDefinition intCoef;

  @Dependency(module = "Equiv")                                  public CoreClassDefinition Equiv;
  @Dependency(module = "Equiv")                                  public CoreClassDefinition QEquiv;
  @Dependency(module = "Equiv.Univalence", name = "Equiv-to-=")  public CoreFunctionDefinition equivToEq;
  @Dependency(module = "Equiv.Univalence", name = "QEquiv-to-=") public CoreFunctionDefinition qEquivToEq;
  @Dependency(module = "Equiv", name = "Map.A")                  CoreClassField equivLeft;
  @Dependency(module = "Equiv", name = "Map.B")                  CoreClassField equivRight;
  @Dependency(module = "Equiv")                                  CoreFunctionDefinition idEquiv;
  @Dependency(module = "Equiv")                                  CoreFunctionDefinition transEquiv;

  public static class TransitivityInstanceCache {
    public final CoreFunctionDefinition instance;
    public final CoreClassField relationField;

    public TransitivityInstanceCache(CoreFunctionDefinition instance, CoreClassField relationField) {
      this.instance = instance;
      this.relationField = relationField;
    }
  }

  public final Map<CoreDefinition, TransitivityInstanceCache> transitivityInstanceCache = new HashMap<>();

  public EquationMeta(StdExtension ext) {
    this.ext = ext;
  }

  @Override
  public @Nullable TypedExpression invokeMeta(@NotNull ExpressionTypechecker typechecker, @NotNull ContextData contextData) {
    ErrorReporter errorReporter = typechecker.getErrorReporter();
    ConcreteReferenceExpression refExpr = contextData.getReferenceExpression();
    ConcreteFactory factory = ext.factory.withData(refExpr.getData());

    // values will contain ConcreteExpression (which correspond to implicit arguments) and TypedExpression (which correspond to explicit arguments).
    List<Object> values = new ArrayList<>();

    EquationSolver solver = null;
    int argIndex = 0;
    List<? extends ConcreteArgument> arguments = contextData.getArguments();
    if (!arguments.isEmpty() && !arguments.get(0).isExplicit()) {
      ConcreteExpression arg = arguments.get(0).getExpression();
      if (arg instanceof ConcreteUniverseExpression) {
        argIndex = 1;
        solver = new EqualitySolver(this, typechecker, factory, refExpr, false);
      } else if (arg instanceof ConcreteReferenceExpression) {
        CoreDefinition def = ext.definitionProvider.getCoreDefinition(((ConcreteReferenceExpression) arg).getReferent());
        if (def instanceof CoreClassDefinition) {
          CoreClassDefinition classDef = (CoreClassDefinition) def;
          if ((classDef.isSubClassOf(Monoid) || classDef.isSubClassOf(AddMonoid) || classDef.isSubClassOf(MSemilattice))) {
            argIndex = 1;
            solver = new EqualitySolver(this, typechecker, factory, refExpr, classDef);
          }
        }
      }
    }

    if (contextData.getExpectedType() != null) {
      CoreExpression type = contextData.getExpectedType().normalize(NormalizationMode.WHNF);
      final List<EquationSolver> solvers = solver != null ? Collections.singletonList(solver) : Arrays.asList(new EqualitySolver(this, typechecker, factory, refExpr), new EquivSolver(this, typechecker, factory), new TransitivitySolver(this, typechecker, factory, refExpr));
      solver = null;
      for (EquationSolver eqSolver : solvers) {
        if (eqSolver.isApplicable(type)) {
          solver = eqSolver;
          break;
        }
      }
      if (solver == null) {
        errorReporter.report(new TypeError("Unrecognized type", type, refExpr));
        return null;
      }
    } else if (solver == null) {
      solver = new EqualitySolver(this, typechecker, factory, refExpr);
    }

    for (; argIndex < arguments.size(); argIndex++) {
      ConcreteArgument argument = arguments.get(argIndex);
      if (argument.isExplicit()) {
        TypedExpression value = typechecker.typecheck(argument.getExpression(), null);
        if (value == null) {
          return null;
        }
        values.add(value);
      } else {
        values.add(argument.getExpression());
      }
    }

    CoreExpression leftExpr = solver.getLeftValue();
    if (leftExpr != null && (values.isEmpty() || !(values.get(0) instanceof TypedExpression) || !Utils.safeCompare(typechecker, leftExpr, ((TypedExpression) values.get(0)).getExpression(), CMP.EQ, refExpr, false, true))) {
      values.add(0, leftExpr.computeTyped());
    }
    CoreExpression rightExpr = solver.getRightValue();
    if (rightExpr != null && (values.isEmpty() || !(values.get(values.size() - 1) instanceof TypedExpression) || !Utils.safeCompare(typechecker, rightExpr, ((TypedExpression) values.get(values.size() - 1)).getExpression(), CMP.EQ, refExpr, false, true))) {
      values.add(rightExpr.computeTyped());
    }

    // If values.size() <= 1, then we don't have expected type
    if (values.isEmpty()) {
      errorReporter.report(new TypecheckingError("Cannot infer type of the expression", refExpr));
      return null;
    }

    // If values.size() == 1, we either return the implicit argument or the trivial answer on the explicit argument
    if (values.size() == 1) {
      return values.get(0) instanceof ConcreteExpression
        ? typechecker.typecheck((ConcreteExpression) values.get(0), null)
        : solver.getTrivialResult((TypedExpression) values.get(0));
    }

    boolean hasMissingProofs = false;
    for (int i = 0; i < values.size(); i++) {
      if (values.get(i) instanceof TypedExpression && i + 1 < values.size() && values.get(i + 1) instanceof TypedExpression) {
        hasMissingProofs = true;
      } else if (values.get(i) instanceof ConcreteExpression) {
        ConcreteExpression expr = (ConcreteExpression) values.get(i);
        if (solver.isHint(expr instanceof ConcreteGoalExpression ? ((ConcreteGoalExpression) expr).getExpression() : expr)) {
          if (i > 0 && values.get(i - 1) instanceof TypedExpression && i + 1 < values.size() && values.get(i + 1) instanceof TypedExpression) {
            hasMissingProofs = true;
          } else {
            errorReporter.report(new TypecheckingError("Hints must be between explicit arguments", (ConcreteExpression) values.get(i)));
            values.remove(i--);
          }
        }
      }
    }

    if (hasMissingProofs) {
      if (solver instanceof EqualitySolver && contextData.getExpectedType() == null) {
        for (Object value : values) {
          if (value instanceof TypedExpression) {
            ((EqualitySolver) solver).setValuesType(((TypedExpression) value).getType());
            break;
          }
        }
      }

      if (!solver.initializeSolver()) {
        errorReporter.report(new TypecheckingError("Cannot infer missing proofs", refExpr));
        return null;
      }
    }

    boolean ok = true;
    List<ConcreteExpression> equalities = new ArrayList<>();
    for (int i = 0; i < values.size(); i++) {
      Object value = values.get(i);
      if (value instanceof ConcreteExpression && solver.isHint((ConcreteExpression) value) || value instanceof TypedExpression && i + 1 < values.size() && values.get(i + 1) instanceof TypedExpression) {
        ConcreteGoalExpression goalExpr = value instanceof ConcreteGoalExpression ? (ConcreteGoalExpression) value : null;
        List<GeneralError> errors = goalExpr != null ? new ArrayList<>() : null;
        ConcreteExpression result = solver.solve(
            goalExpr != null ? goalExpr.getExpression() : value instanceof ConcreteExpression ? (ConcreteExpression) value : null,
            (TypedExpression) values.get(value instanceof ConcreteExpression ? i - 1 : i),
            (TypedExpression) values.get(i + 1),
            errors != null ? new ListErrorReporter(errors) : errorReporter);
        if (result == null && goalExpr == null) {
          ok = false;
          continue;
        }
        equalities.add(goalExpr == null ? result : factory.withData(goalExpr.getData()).goal(goalExpr.getName(), result, null, Objects.requireNonNull(errors)));
      } else if (value instanceof ConcreteExpression) {
        Maybe<CoreExpression> eqType = solver.getEqType(
            i > 0 && values.get(i - 1) instanceof TypedExpression ? (TypedExpression) values.get(i - 1) : null,
            i < values.size() - 1 && values.get(i + 1) instanceof TypedExpression ? (TypedExpression) values.get(i + 1) : null);

        TypedExpression result = eqType == null ? null : typechecker.typecheck((ConcreteExpression) value, eqType.just);
        if (result == null) {
          ok = false;
        } else {
          equalities.add(factory.core(null, result));
        }
      }
    }
    if (!ok) {
      return null;
    }

    ConcreteExpression result = equalities.get(equalities.size() - 1);
    for (int i = equalities.size() - 2; i >= 0; i--) {
      result = solver.combineResults(equalities.get(i), result);
    }
    return hasMissingProofs ? solver.finalize(result) : typechecker.typecheck(result, null);
  }
}
