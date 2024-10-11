package com.idle.kb_i_dle_backend.domain.finance.service;

import com.idle.kb_i_dle_backend.domain.finance.dto.BondDTO;
import java.text.ParseException;
import java.util.List;

public interface BondService {
    List<BondDTO> getBondList(Integer uid) throws Exception;

    BondDTO addBond(Integer uid, BondDTO bondDTO) throws ParseException;

    BondDTO updateBond(Integer uid, BondDTO bondDTO) throws ParseException;

    BondDTO deleteBond(Integer uid, Integer index) throws ParseException;
}
