package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.service.NodeSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/node")
public class NodeController {


    @GetMapping("/{id}/active={active}")
    public void setActive(@PathVariable UUID id, @PathVariable Boolean active) {

    }

}
