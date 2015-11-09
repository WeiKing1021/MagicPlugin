package com.elmakers.mine.bukkit.batch;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.BlockData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.block.BlockList;
import com.elmakers.mine.bukkit.block.UndoList;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class UndoBatch implements com.elmakers.mine.bukkit.api.batch.UndoBatch {
    protected final MageController controller;
    private BlockList trackUndoBlocks;
    protected boolean finished = false;
    protected boolean applyPhysics = false;
    protected UndoList undoList;
    protected int listSize;

    private final Set<Material> attachables;

    public UndoBatch(UndoList blockList) {
        Mage mage = blockList.getOwner();
        controller = mage.getController();

        // We're going to track the blocks we undo
        // This is just for updating dynmap and other Controller-level
        // block change triggers
        trackUndoBlocks = new BlockList();

        undoList = blockList;
        this.applyPhysics = blockList.getApplyPhysics();
        this.attachables = new HashSet<Material>();

        Set<Material> addToAttachables = controller.getMaterialSet("attachable");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }
        addToAttachables = controller.getMaterialSet("attachable_wall");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }
        addToAttachables = controller.getMaterialSet("attachable_double");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }
        addToAttachables = controller.getMaterialSet("delayed");
        if (addToAttachables != null) {
            attachables.addAll(addToAttachables);
        }

        // Sort so attachable items don't break

        org.bukkit.Bukkit.getLogger().info("SIZE: " + undoList.size());

        undoList.sort(attachables);
        listSize = undoList.size();

        org.bukkit.Bukkit.getLogger().info("SIZE post-sort: " + undoList.size());
    }

    public int size() {
        return listSize;
    }

    public int remaining() {
        return undoList == null ? 0 : undoList.size();
    }

    public int process(int maxBlocks) {
        org.bukkit.Bukkit.getLogger().info("Processing up to " + maxBlocks + " of " + undoList.size());


        int processedBlocks = 0;
        while (undoList.size() > 0 && processedBlocks < maxBlocks) {
            BlockData undone = undoList.undoNext(applyPhysics);
            if (undone == null) {
                break;
            }
            trackUndoBlocks.add(undone);
            processedBlocks++;
        }
        if (undoList.size() == 0) {
            finish();
        }

        return processedBlocks;
    }

    public void finish() {
        if (!finished) {
            finished = true;
            undoList.unregisterAttached();
            undoList.undoEntityEffects();
            controller.update(trackUndoBlocks);
            trackUndoBlocks = null;
            CastContext context = undoList.getContext();
            if (context != null) {
                context.playEffects("undo");
            }
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public String getName() {
        return "Undo " + undoList.getName();
    }
}
