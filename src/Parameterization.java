package org.moeaframework.analysis.diagnostics;

/** The data type representing a BORG parameterization. */
public class Parameterization {

	public double sbxRate;

	public double sbxDistributionIndex;

	public double pmRate;

	public double pmDistributionIndex;

	public double deCrossoverRate;

	public double deStepSize;

	public double umRate;

	public double spxEpsilon;

	public int spxParents;

	public int spxOffspring;

	public double pcxEta;

	public double pcxZeta;

	public int pcxParents;

	public int pcxOffspring;

	public double undxZeta;

	public double undxEta;

	public int undxParents;

	public int undxOffspring;

	public Parameterization(double sbxr, double sbxdi, double pmr,
		double pmdi, double dexr, double des, double umr, double spxe,
		int spxp, int spxo, double pcxe, double pcxz, int pcxp, int pcxo,
		double undxz, double undxe, int undxp, int undxo) {
		sbxRate= sbxr;
		sbxDistributionIndex= sbxdi;
		pmRate= pmr;
		pmDistributionIndex= pmdi;
		deCrossoverRate= dexr;
		deStepSize= des;
		umRate= umr;
		spxEpsilon= spxe;
		spxParents= spxp;
		spxOffspring= spxo;
		pcxEta= pcxe;
		pcxZeta= pcxz;
		pcxParents= pcxp;
		pcxOffspring= pcxo;
		undxEta= undxe;
		undxZeta= undxz;
		undxParents= undxp;
		undxOffspring= undxo;

	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) { return false; }
		Parameterization p= (Parameterization) o;
		return sbxRate == p.sbxRate && sbxDistributionIndex == p.sbxDistributionIndex &&
			pmRate == p.pmRate && pmDistributionIndex == p.pmDistributionIndex &&
			deCrossoverRate == p.deCrossoverRate && deStepSize == p.deStepSize &&
			umRate == p.umRate && spxEpsilon == p.spxEpsilon && spxParents == p.spxParents &&
			spxOffspring == p.spxOffspring && pcxEta == p.pcxEta && pcxZeta == p.pcxZeta &&
			pcxParents == p.pcxParents && pcxOffspring == p.pcxOffspring && undxEta == p.undxEta &&
			undxZeta == p.pcxZeta && pcxParents == p.undxParents &&
			undxOffspring == p.undxOffspring;
	}

	@Override
	public String toString() {
		return Double.toString(sbxRate) + Double.toString(sbxDistributionIndex) +
			Double.toString(pmRate) + Double.toString(pmDistributionIndex) +
			Double.toString(deCrossoverRate) + Double.toString(deStepSize) +
			Double.toString(umRate) + Double.toString(spxEpsilon) +
			Integer.toString(spxParents) + Integer.toString(spxOffspring) +
			Double.toString(pcxEta) + Double.toString(pcxZeta) +
			Integer.toString(pcxParents) + Integer.toString(pcxOffspring) +
			Double.toString(undxZeta) + Double.toString(undxEta) +
			Integer.toString(undxParents) + Integer.toString(undxParents);
	}
}
