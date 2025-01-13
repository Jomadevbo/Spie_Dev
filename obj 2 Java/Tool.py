def obj_to_java_format(obj_path, output_path):
    vertices = []
    lines = set()
    faces = []

    with open(obj_path, 'r') as file:
        for line in file:
            parts = line.strip().split()
            if not parts:
                continue
            if parts[0] == 'v':  # Vertex
                x, y, z = map(float, parts[1:4])
                vertices.append((x, y, z))
            elif parts[0] == 'f':  # Face
                face_indices = [int(idx.split('/')[0]) - 1 for idx in parts[1:]]
                faces.append(face_indices)
                for i in range(len(face_indices)):
                    start = face_indices[i]
                    end = face_indices[(i + 1) % len(face_indices)]
                    lines.add(tuple(sorted((start, end))))

    # Convert to Java-friendly text format
    vert_table = f"double[][] VertTable = {{\n"
    vert_table += ",\n".join(
        f"    {{ {v[0]}, {v[1]}, {v[2]} }}" for v in vertices
    ) + "\n};\n"

    line_table = f"int[][] LineTable = {{\n"
    line_table += ",\n".join(
        f"    {{ {line[0]}, {line[1]} }}" for line in sorted(lines)
    ) + "\n};\n"

    face_table = f"int[][] FaceTable = {{\n"
    face_table += ",\n".join(
        f"    {{ {', '.join(map(str, face))} }}" for face in faces
    ) + "\n};\n"

    with open(output_path, 'w') as output_file:
        output_file.write(vert_table + "\n")
        output_file.write(line_table + "\n")
        output_file.write(face_table + "\n")

    print(f"Converted {obj_path} to Java format and saved to {output_path}")


# Example usage
if __name__ == "__main__":
    obj_to_java_format("untitled.obj", "java_model_format.txt")
