# Inspecting Parquet

```bash
# Metadata
parq  output-00000-of-00005.parquet

data/output/beam/output-00001-of-00002.parquet

parq data/output/beam/output-00001-of-00002.parquet

# Schema
parq  output-00000-of-00003.parquet  --schema

parq  data/output/beam/output-00001-of-00002.parquet  --schema

# Top n Rows
parq data/output/beam/parquet/output-00000-of-00005.parquet --head 10

```