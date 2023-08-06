package fr.rader.imbob.packets.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataBlockArray implements Iterable<DataBlock> {

    private final List<DataBlock> dataBlocks;

    public DataBlockArray() {
        this.dataBlocks = new ArrayList<>();
    }

    public DataBlock create() {
        DataBlock block = new DataBlock();

        this.dataBlocks.add(block);

        return block;
    }

    public int size() {
        return this.dataBlocks.size();
    }

    @Override
    public Iterator<DataBlock> iterator() {
        return this.dataBlocks.iterator();
    }
}
