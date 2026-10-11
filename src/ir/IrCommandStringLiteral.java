package ir;

import mips.MipsGenerator;

public class IrCommandStringLiteral extends IrCommand
{
    public final String label;
    public final String value; // raw java string (not escaped)

    public IrCommandStringLiteral(String label, String value)
    {
        this.label = label;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "string " + label + " = \"" + value + "\"";
    }

    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().allocateString(label, value);
    }
}
