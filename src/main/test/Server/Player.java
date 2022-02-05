package Server;

import com.ethereal.meta.meta.annotation.Components;
import com.ethereal.meta.standard.StandardMeta;

@Components(meta = StandardMeta.class)
public class Player {
    private String name;
    private Long id;
}
