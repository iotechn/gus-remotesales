package com.dobbinsoft.gus.remotesales.service.impl;

import com.dobbinsoft.gus.remotesales.data.enums.BaseEnums;
import com.dobbinsoft.gus.remotesales.data.vo.basic.BaseEnumVO;
import com.dobbinsoft.gus.remotesales.service.BasicService;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class BasicServiceImpl implements BasicService {

    private static final String ENUM_SCAN_PACKAGE = "com.dobbinsoft.gus.remotesales.data.enums";

    private final AtomicReference<List<BaseEnumVO>> resultCache = new AtomicReference<>(new ArrayList<>());

    @Override
    public List<BaseEnumVO> enums() {
        List<BaseEnumVO> current = resultCache.get();
        if (current.isEmpty()) {
            List<BaseEnumVO> newValue = getEnums();
            if (resultCache.compareAndSet(current, newValue)) {
                return newValue;
            }
            return resultCache.get();
        }
        return current;
    }

    private List<BaseEnumVO> getEnums() {
        List<BaseEnumVO> result = new ArrayList<>();
        try {
            Reflections reflections = new Reflections(ENUM_SCAN_PACKAGE);
            Set<Class<? extends BaseEnums>> subTypes = reflections.getSubTypesOf(BaseEnums.class);
            for (Class<? extends BaseEnums> clazz : subTypes) {
                if (clazz.isEnum()) {
                    BaseEnumVO baseEnumVO = new BaseEnumVO();
                    baseEnumVO.setEnumName(clazz.getSimpleName());
                    List<BaseEnumVO.Item> items = new ArrayList<>();
                    for (Object enumConstant : clazz.getEnumConstants()) {
                        BaseEnums<?> baseEnum = (BaseEnums<?>) enumConstant;
                        BaseEnumVO.Item item = new BaseEnumVO.Item();
                        item.setCode(baseEnum.getCode());
                        item.setDesc(baseEnum.getMsg());
                        items.add(item);
                    }
                    baseEnumVO.setItems(items);
                    result.add(baseEnumVO);
                }
            }
        } catch (Exception e) {
            log.error("[获取枚举] 异常", e);
        }

        return result;
    }

}
