package com.dante.diary.draw;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;

import com.byox.drawview.enums.DrawingCapture;
import com.byox.drawview.enums.DrawingMode;
import com.byox.drawview.enums.DrawingTool;
import com.byox.drawview.views.DrawView;
import com.dante.diary.R;
import com.dante.diary.base.BaseActivity;
import com.dante.diary.custom.PickPictureActivity;
import com.dante.diary.draw.dialogs.DrawAttribsDialog;
import com.dante.diary.draw.dialogs.RequestTextDialog;
import com.dante.diary.draw.dialogs.SelectChoiceDialog;
import com.dante.diary.utils.BitmapUtil;
import com.dante.diary.utils.UiUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;

import butterknife.BindView;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.dante.diary.custom.PickPictureActivity.REQUEST_PICK_PICTURE;

public class DrawActivity extends BaseActivity {

    @BindView(R.id.drawView)
    DrawView mDrawView;

    @BindView(R.id.fabMenu)
    FabSpeedDial fabMenu;
    private MenuItem mMenuItemRedo;
    private MenuItem mMenuItemUndo;
    private File photoFile;

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        init();
    }

    private void init() {
        fabMenu.setMenuListener(new SimpleMenuListenerAdapter() {

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.fab_draw_clear:
                        clearDraw();
                        break;
                    case R.id.fab_draw_attrs:
                        changeDrawAttribs();
                        break;
//                    case R.id.fab_draw_mode:
//                        changeDrawMode();
//                        break;
                    case R.id.fab_draw_background:
                        chooseBackgroundImage();
                        break;
                    case R.id.fab_draw_tool:
                        changeDrawTool();
                        break;
                    case R.id.fab_draw_save:
                        RxPermissions permissions = new RxPermissions(DrawActivity.this);
                        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(grant -> {
                                    if (grant) {
                                        saveDraw();
                                    } else {
                                        UiUtils.showSnack(getWindow().getDecorView(), getString(R.string.save_img_failed));
                                    }
                                });
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        setListeners();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_draw;
    }

    @Override
    public void onBackPressed() {
        if (mDrawView.canUndo()) {
            new AlertDialog.Builder(DrawActivity.this)
                    .setMessage(R.string.draw_exit_message)
                    .setPositiveButton(R.string.exit, (dialog, which) -> DrawActivity.super.onBackPressed())
                    .setNegativeButton(R.string.save, (dialog2, which) -> saveDraw()).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_draw_undo, menu);
        mMenuItemUndo = menu.getItem(0);
        mMenuItemRedo = menu.getItem(1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo:
                if (mDrawView.canUndo()) {
                    mDrawView.undo();
                    canUndoRedo();
                }
                break;
            case R.id.action_redo:
                if (mDrawView.canRedo()) {
                    mDrawView.redo();
                    canUndoRedo();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        mDrawView.setOnDrawViewListener(new DrawView.OnDrawViewListener() {
            @Override
            public void onStartDrawing() {
                canUndoRedo();
            }

            @Override
            public void onEndDrawing() {
                canUndoRedo();
            }

            @Override
            public void onClearDrawing() {
                canUndoRedo();
            }

            @Override
            public void onRequestText() {
                RequestTextDialog requestTextDialog =
                        RequestTextDialog.newInstance("");
                requestTextDialog.setOnRequestTextListener(new RequestTextDialog.OnRequestTextListener() {
                    @Override
                    public void onRequestTextConfirmed(String requestedText) {
                        mDrawView.refreshLastText(requestedText);
                    }

                    @Override
                    public void onRequestTextCancelled() {
                        mDrawView.cancelTextRequest();
                    }
                });
                requestTextDialog.show(getSupportFragmentManager(), "requestText");
            }

            @Override
            public void onAllMovesPainted() {

            }
        });


    }


    private void changeDrawTool() {
        SelectChoiceDialog selectChoiceDialog =
                SelectChoiceDialog.newInstance(getString(R.string.chose_draw_tool),
                        getResources().getStringArray(R.array.draw_tools));
        selectChoiceDialog.setOnChoiceDialogListener(position -> mDrawView.setDrawingTool(DrawingTool.values()[position]));
        selectChoiceDialog.show(getSupportFragmentManager(), "choiceDialog");
    }

    private void changeDrawMode() {
        SelectChoiceDialog selectChoiceDialog =
                SelectChoiceDialog.newInstance(getString(R.string.choose_draw_mode),
                        getResources().getStringArray(R.array.draw_mode));
        selectChoiceDialog.setOnChoiceDialogListener(position -> mDrawView.setDrawingMode(DrawingMode.values()[position]));
        selectChoiceDialog.show(getSupportFragmentManager(), "choiceDialog");
    }

    private void changeDrawAttribs() {
        DrawAttribsDialog drawAttribsDialog = DrawAttribsDialog.newInstance();
        drawAttribsDialog.setPaint(mDrawView.getCurrentPaintParams());
        drawAttribsDialog.setOnCustomViewDialogListener(newPaint -> {
            mDrawView.setDrawColor(newPaint.getColor())
                    .setPaintStyle(newPaint.getStyle())
                    .setDither(newPaint.isDither())
                    .setDrawWidth((int) newPaint.getStrokeWidth())
                    .setDrawAlpha(newPaint.getAlpha())
                    .setAntiAlias(newPaint.isAntiAlias())
                    .setLineCap(newPaint.getStrokeCap())
                    .setFontFamily(newPaint.getTypeface())
                    .setFontSize(newPaint.getTextSize());
//                If you prefer, you can easily refresh new attributes using this method
//                mDrawView.refreshAttributes(newPaint);
        });
        drawAttribsDialog.show(getSupportFragmentManager(), "drawAttribs");
    }

    private void saveDraw() {
        Object[] draw = mDrawView.createCapture(DrawingCapture.BITMAP);
        File file = BitmapUtil.writeToFile((Bitmap) draw[0]);
        if (file.exists()) {
            Snackbar.make(fabMenu, R.string.draw_saved_success, Snackbar.LENGTH_SHORT).show();
        }
        Intent data = new Intent();
        data.putExtra("path", file.getAbsolutePath());
        setResult(RESULT_OK, data);
        finish();
    }

    private void chooseBackgroundImage() {
        startActivityForResult(new Intent(getApplicationContext(), PickPictureActivity.class), REQUEST_PICK_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = data.getStringExtra("path");
                onSelectImage(new File(path));
            } else if (resultCode == PickPictureActivity.RESULT_FAILED) {
                UiUtils.showSnack(fabMenu, getString(R.string.fail_read_pictures));
            }

        }
    }

    public void onSelectImage(File imageFile) {
        mDrawView.setBackgroundImage(imageFile);
    }

    private void clearDraw() {
        mDrawView.restartDrawing();
    }

    private void canUndoRedo() {
        if (!mDrawView.canUndo()) {
            mMenuItemUndo.setEnabled(false);
            mMenuItemUndo.setIcon(R.drawable.ic_action_content_undo_disabled);
        } else {
            mMenuItemUndo.setEnabled(true);
            mMenuItemUndo.setIcon(R.drawable.ic_action_content_undo);
        }
        if (!mDrawView.canRedo()) {
            mMenuItemRedo.setEnabled(false);
            mMenuItemRedo.setIcon(R.drawable.ic_action_content_redo_disabled);
        } else {
            mMenuItemRedo.setEnabled(true);
            mMenuItemRedo.setIcon(R.drawable.ic_action_content_redo);
        }
    }

}
