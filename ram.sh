ram_base_dir="$1"
project_name="$2"

echo "RAM base directory path: $ram_base_dir"
echo "project directory name: $project_name"

if [[ -d "$ram_base_dir/$project_name" ]]
then
	echo "$ram_base_dir/$project_name is already exist"
else
	mkdir $ram_base_dir/$project_name
	mkdir $ram_base_dir/$project_name/target
fi

if [[ -d "target" && -h "target" ]]
then
	echo "target symlink is already exist"
else
	rm -r target
	ln -s $ram_base_dir/$project_name/target target
fi
