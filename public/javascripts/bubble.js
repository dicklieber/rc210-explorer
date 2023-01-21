d3.json(svgData, function (error, graph) {
// d3.json("/bubbleSvg", function (error, graph) {
    if (error) {
        let source = error.source;
        let data = error.data;
        throw error;
    }
    const nodes = graph.nodes
    const bubbleLinks = graph.links

    bubbleLinks.forEach(function (d) {
        d.source = d.source_id;
        d.target = d.target_id;
    });

    var svg = d3.select("svg"),
        width = +svg.attr("width"),
        height = +svg.attr("height");

    //add encompassing group for the zoom
    let topG = svg.append("g")
        .attr("class", "everything");


    //Create the link force
//We need the id accessor to use named sources and targets

//set up the simulation
//nodes only for now
    var simulation = d3.forceSimulation()


//add forces
//we're going to add a charge to each node
//also going to add a centering force
    simulation
        .force('collision', d3.forceCollide()
            .radius(85)
            .iterations(16)
        )
        // .force("charge_force", d3.forceManyBody())
        .force("center_force", d3.forceCenter(width / 2, height / 2))
        .force("link", d3.forceLink().id(function (d) {
            return d.nodeId}
        ));


//Add a bubbleLinks force to the simulation


//draw lines for the bubbleLinks
    var link = topG.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(bubbleLinks)
        .enter().append("line")
        .attr("stroke-width", 2);


    // let linkForce = simulation.force("link");
    // linkForce.links(bubbleLinks);

//draw circles for the nodes
    var node = topG.append("g")

        .attr("class", "nodes")
        .selectAll("g")
        .data(nodes)
        .enter()
        .append("g")
    // .attr("rx", 30)
    // .attr("ry", 15)
    // .attr("fill", "red");

    node.append("ellipse")
        .attr("class", function(d){
           return d.cssClass
        })
        .attr("rx", 85)
        .attr("ry", 35)
        .on("click", function(arg){
            console.log(`arg: ${arg.nodeId}`)
            showDetail(arg.nodeId)
            // let url = `http://127.0.0.1:9000/nodeDetail/${arg.nodeId}`;
            // $("#details").load(url);
        })

    let text = node.append("text")
        .attr("text-anchor", "middle")
        // .attr("x", 0)
        .attr("dy", ".35em")
        .attr("dx", function (d) {
            return 0;
        });
    /*
            .text(function (d) {
                    return d.nodeId
                }
            )
    */
    let nodeTspan =   text.append("tspan").text(function (d) {
        return d.top.text
    })
        .attr("class", function (d) {
            return d.top.cssClass
        })
        .attr("dy", -15)
        .attr("x", 0)



    text.append("tspan").text(function (d) {
        return d.middle.text
    })
        .attr("class", function (d) {
                return d.middle.cssClass
            })
        .attr("dy", "14pt")
        .attr("x", 0)


    text.append("tspan").text(function (d) {
        return d.bottom.text
    })
        .attr("class", function (d) {
            return d.bottom.cssClass
        })
        .attr("dy", "14pt")
        .attr("x", 0)


//add tick instructions:
    simulation.nodes(nodes)
        .on("tick", tickActions)
    simulation.force("link").links(bubbleLinks)



    function tickActions() {
        // nodes[0].x = w / 2;
        // nodes[0].y = h / 2;
        //update link positions
        //simply tells one end of the line to follow one node around
        //and the other end of the line to follow the other node around
        link
            .attr("x1", function (d) {
                return d.source.x;
            })
            .attr("y1", function (d) {
                return d.source.y;
            })
            .attr("x2", function (d) {
                return d.target.x;
            })
            .attr("y2", function (d) {
                return d.target.y;
            });
        //update circle positions each tick of the simulation
        node.attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
        })

    }

    //Zoom functions
    function zoom_actions() {
        topG.attr("transform", d3.event.transform)
    }

//add zoom capabilities
    var zoom_handler = d3.zoom()
        .on("zoom", zoom_actions);

    zoom_handler(svg);


//Drag functions
//d is the node
    function drag_start(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

//make sure you can't drag the circle outside the box
    function drag_drag(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

    function drag_end(d) {
        if (!d3.event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }


//add drag capabilities
    var drag_handler = d3.drag()
        .on("start", drag_start)
        .on("drag", drag_drag)
        .on("end", drag_end);

    drag_handler(node);

});