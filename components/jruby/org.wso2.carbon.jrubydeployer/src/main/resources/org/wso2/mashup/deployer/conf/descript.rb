
module Annotation
  class AnnoDesc
	@@key_no=1
	@@debug=false
   def set_anno hs
	i = 1
	
		
	temp_key = "key#{@@key_no.to_s}"
	@annoMap.store(temp_key,hs)
	#puts hs
	@@key_no = @@key_no + 1
	
    end
    
    def _anno *args
	if @@debug==true
		 puts "annotations set successfully"
	end

	if(!args.nil? and args.size>0 and args[0].is_a? Hash)  
		set_anno args[0]
	else
		raise RuntimeError, "Annotation Syntax Error"
	end
	
    end
	
    def getMap
	@annoMap
    end


    def print
	p = Proc.new { |key,val|;if val.is_a? Hash;puts "hash map present";puts "key: #{key} ";end}
    end
  

    def print_map hs
	hs.each do |key,val|
		if val.is_a? Hash
                       if @@debug==true; puts "hash map present" end
			puts "key: #{key} "
			print_map val
		else
			puts "key: #{key} <--> val: #{val}" 
		end									
										 
	end 
    end

    

    def to_s
       print_map @annoMap

    end
 
    private
    def initialize
	@annoMap = {}
	
    end
 
    def self.debug= is_on
	@@debug = is_on
    end
    
 
    def self.instance
      @@instance ||= new
    end

  end
end
 
def _anno(*args)

  Annotation::AnnoDesc.instance._anno *args
  
end

def _anno_out

  Annotation::AnnoDesc.instance
end

def _anno_debug(is_on)

  Annotation::AnnoDesc.debug = is_on
end
