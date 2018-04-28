package net.glowstone.io.structure;

import net.glowstone.generator.structures.GlowTemplePiece;
import net.glowstone.util.nbt.CompoundTag;

abstract class TemplePieceStore<T extends GlowTemplePiece> extends StructurePieceStore<T> {

    public TemplePieceStore(Class<T> clazz, String id) {
        super(clazz, id);
    }

    @Override
    public void load(T structurePiece, CompoundTag compound) {
        super.load(structurePiece, compound);
        compound.readInt(structurePiece::setWidth, "Width");
        compound.readInt(structurePiece::setHeight, "Height");
        compound.readInt(structurePiece::setDepth, "Depth");
        compound.readInt(structurePiece::setHorizPos, "HPos");
    }

    @Override
    public void save(T structurePiece, CompoundTag compound) {
        super.save(structurePiece, compound);

        compound.putInt("Width", structurePiece.getWidth());
        compound.putInt("Height", structurePiece.getHeight());
        compound.putInt("Depth", structurePiece.getDepth());
        compound.putInt("HPos", structurePiece.getHorizPos());
    }
}
