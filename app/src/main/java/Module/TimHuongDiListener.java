package Module;

import java.util.List;

public interface TimHuongDiListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<HuongDi> route);
}
