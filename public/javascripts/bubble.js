// Bubble view of flow
var svg = d3.select("svg"),
    width = +svg.attr("width"),
    height = +svg.attr("height");

//add encompassing group for the zoom
let topG = svg.append("g")
    .attr("class", "everything");

var color = d3.scaleOrdinal(d3.schemeCategory20);
var simulation = d3.forceSimulation()
    .force("link", d3.forceLink().id(function (d) {
        return d.id;
    }))
    .force("charge", d3.forceManyBody())
    .force("center", d3.forceCenter(width / 2, height / 2))
    .force('collision', d3.forceCollide()
        .radius(85)
        .iterations(16)
    )
;
d3.json("/bubbleData", function (error, graph) {
    if (error) throw error;

    var link = svg.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(graph.links)
        .enter().append("line")
        .attr("stroke-width", function (d) {
            return Math.sqrt(d.value);
        });

    var node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("g")
        .data(graph.nodes)
        .enter().append("g")
    var nodeRects = node.append("rect").attr("rx", 6)
        .attr("ry", 6)
        .attr("x", -12.5)
        .attr("y", -12.5)
        .attr("width", 95)
        .attr("height", 25)
        .attr("fill", function (d) {
            return color(d.group);
        });

// Create a drag handler and append it to the node object instead
    var drag_handler = d3.drag()
        .on("start", dragstarted)
        .on("drag", dragged)
        .on("end", dragended);
    drag_handler(node);
    var lables = node.append("text")
        .text(function (d) {
            return d.info;
        })
        .attr('x', 6)
        .attr('y', 3);
    node.append("title")
        .text(function (d) {
            return d.id;
        });
    simulation
        .nodes(graph.nodes)
        .on("tick", ticked);
    simulation.force("link")
        .links(graph.links);

    //Zoom functions
    function zoom_actions() {
        topG.attr("transform", d3.event.transform)
    }

//add zoom capabilities
    var zoom_handler = d3.zoom()
        .on("zoom", zoom_actions);

    zoom_handler(svg);


    function ticked() {
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
        node
            .attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            })
    }
});

function dragstarted(d) {
    if (!d3.event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(d) {
    d.fx = d3.event.x;
    d.fy = d3.event.y;
}

function dragended(d) {
    if (!d3.event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}
