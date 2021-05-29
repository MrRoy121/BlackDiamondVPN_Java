package com.blackdiamond.apps.studio.vpn.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.blackdiamond.apps.studio.vpn.R;
import com.blackdiamond.apps.studio.vpn.model.Server;
import com.blackdiamond.apps.studio.vpn.utils.OvpnUtils;

import java.util.ArrayList;
import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {

    private List<Server> servers = new ArrayList<>();

    private ServerClickCallback callback;

    public ServerAdapter(List<Server> servers, @NonNull ServerClickCallback callback) {
        this.servers.clear();
        this.servers.addAll(servers);
        this.callback = callback;
    }

    public void setServerList(@NonNull final List<Server> serverList) {
        if (servers.isEmpty()) {
            servers.clear();
            servers.addAll(serverList);
            notifyItemRangeInserted(0, serverList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return servers.size();
                }

                @Override
                public int getNewListSize() {
                    return serverList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Server old = servers.get(oldItemPosition);
                    Server server = serverList.get(newItemPosition);
                    return old.hostName.equals(server.hostName);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Server old = servers.get(oldItemPosition);
                    Server server = serverList.get(newItemPosition);
                    return old.hostName.equals(server.hostName)
                            && old.ipAddress.equals(server.ipAddress)
                            && old.countryLong.equals(server.countryLong);
                }
            });
            servers.clear();
            servers.addAll(serverList);
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.server_list_item, parent, false);
        return new ViewHolder(view, callback);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(servers.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return servers == null ? 0 : servers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View rootView;
        final TextView countryView;
        final TextView protocolView;
        final TextView ipAddressView;
        final TextView speedView;
        final TextView pingView;
        final ImageView img;

        final ServerClickCallback callback;

        public ViewHolder(View view, ServerClickCallback callback) {
            super(view);
            rootView = view;
            countryView = view.findViewById(R.id.tv_country_name);
            protocolView = view.findViewById(R.id.tv_protocol);
            ipAddressView = view.findViewById(R.id.tv_ip_address);
            speedView = view.findViewById(R.id.tv_speed);
            pingView = view.findViewById(R.id.tv_ping);
            img = view.findViewById(R.id.server_flag_image);
            this.callback = callback;
        }

        public void bind(@NonNull final Server server) {
            final Context context = rootView.getContext();

            countryView.setText(server.countryLong);
            if(server.countryShort.equals("JP")){
                img.setImageResource(R.drawable.japan);
            }else if(server.countryShort.equals("US")){
                img.setImageResource(R.drawable.unitedstates);
            }else if(server.countryShort.equals("KR")){
                img.setImageResource(R.drawable.southkorea);
            }else if(server.countryShort.equals("RU")){
                img.setImageResource(R.drawable.russia);
            }else if(server.countryShort.equals("VE")){
                img.setImageResource(R.drawable.venezuela);
            }else if(server.countryShort.equals("TH")){
                img.setImageResource(R.drawable.thailand);
            }else if(server.countryShort.equals("HK")){
                img.setImageResource(R.drawable.hongkong);
            }else if(server.countryShort.equals("CO")){
                img.setImageResource(R.drawable.cambodia);
            }else if(server.countryShort.equals("VN")){
                img.setImageResource(R.drawable.vietnam);
            }else if(server.countryShort.equals("MX")){
                img.setImageResource(R.drawable.mexico);
            }else if(server.countryShort.equals("CA")){
                img.setImageResource(R.drawable.canada);
            }else if(server.countryShort.equals("AR")){
                img.setImageResource(R.drawable.argentina);
            }
                Log.e("sSS", server.countryShort);
            protocolView.setText(server.protocol.toUpperCase());
            ipAddressView.setText(context.getString(R.string.format_ip_address,
                    server.ipAddress, server.port));
            speedView.setText(context.getString(R.string.format_speed,
                    OvpnUtils.humanReadableCount(server.speed, true)));
            pingView.setText(context.getString(R.string.format_ping, server.ping));
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onItemClick(server);
                }
            });
        }
    }

    public interface ServerClickCallback {
        void onItemClick(@NonNull Server server);
    }
}
