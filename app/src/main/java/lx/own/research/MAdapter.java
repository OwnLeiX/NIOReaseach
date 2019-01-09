package lx.own.research;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 * Created on 2019/1/9.
 */
public class MAdapter extends RecyclerView.Adapter<MAdapter.InnerHolder> {

    private List<String> mDatas;

    public void append(String data) {
        if (mDatas == null)
            mDatas = new ArrayList<>();
        mDatas.add(data);
        notifyItemInserted(mDatas.size() - 1);
    }

    public void refresh(List<String> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public InnerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final TextView itemView = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new InnerHolder(itemView, itemView);
    }

    @Override
    public void onBindViewHolder(InnerHolder holder, int position) {
        final String data = getData(position);
        if (holder != null) holder.bind(data);
    }

    public String getData(int position) {
        if (position >= 0 && mDatas != null && mDatas.size() > position) {
            return mDatas.get(position);
        } else {
            return "Exception:position out of bounds.";
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public static class InnerHolder extends RecyclerView.ViewHolder {

        private final TextView mTextView;

        public InnerHolder(View itemView, TextView textView) {
            super(itemView);
            mTextView = textView;
        }

        void bind(String text) {
            mTextView.setText(text);
        }
    }
}
