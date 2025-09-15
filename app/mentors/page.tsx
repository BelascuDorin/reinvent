"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { trpc } from "@/lib/trpc"
import { Users, ArrowLeft, Search, MapPin, Star, Briefcase, Filter } from "lucide-react"
import Link from "next/link"

export default function MentorsPage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  const [filters, setFilters] = useState({
    search: "",
    industry: "Any industry",
    expertise: "Any expertise",
    location: "Any location",
    minRating: undefined as number | undefined,
    sortBy: "rating" as "rating" | "experience" | "reviews",
  })

  const [showFilters, setShowFilters] = useState(false)

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: mentors, isLoading } = trpc.mentor.getMentors.useQuery(filters)
  const { data: filterOptions } = trpc.mentor.getFilterOptions.useQuery()

  const handleFilterChange = (key: string, value: string | number | undefined) => {
    setFilters((prev) => ({ ...prev, [key]: value }))
  }

  const clearFilters = () => {
    setFilters({
      search: "",
      industry: "Any industry",
      expertise: "Any expertise",
      location: "Any location",
      minRating: undefined,
      sortBy: "rating",
    })
  }

  if (status === "loading" || isLoading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div>Loading...</div>
      </div>
    )
  }

  if (!session) {
    return null
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <Link href="/dashboard">
              <Button variant="ghost" size="sm">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back to Dashboard
              </Button>
            </Link>
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <Users className="w-5 h-5 text-primary-foreground" />
              </div>
              <h1 className="text-xl font-semibold text-foreground">Discover Mentors</h1>
            </div>
          </div>
          <Button variant="outline" onClick={() => setShowFilters(!showFilters)}>
            <Filter className="w-4 h-4 mr-2" />
            Filters
          </Button>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Find Your Perfect Mentor</h2>
          <p className="text-muted-foreground">
            Connect with experienced Romanian professionals who can guide your career journey
          </p>
        </div>

        {/* Search and Filters */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle className="flex items-center">
              <Search className="w-5 h-5 mr-2" />
              Search Mentors
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Main Search */}
            <div className="space-y-2">
              <Label>Search by name, expertise, or company</Label>
              <Input
                placeholder="e.g., Software Engineer, Google, Marketing"
                value={filters.search}
                onChange={(e) => handleFilterChange("search", e.target.value)}
              />
            </div>

            {/* Advanced Filters */}
            {showFilters && (
              <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4 pt-4 border-t">
                <div className="space-y-2">
                  <Label>Industry</Label>
                  <Select value={filters.industry} onValueChange={(value) => handleFilterChange("industry", value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Any industry" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Any industry">Any industry</SelectItem>
                      {filterOptions?.industries.map((industry) => (
                        <SelectItem key={industry} value={industry}>
                          {industry}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Expertise</Label>
                  <Select value={filters.expertise} onValueChange={(value) => handleFilterChange("expertise", value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Any expertise" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Any expertise">Any expertise</SelectItem>
                      {filterOptions?.expertise.map((exp) => (
                        <SelectItem key={exp} value={exp}>
                          {exp}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Location</Label>
                  <Select value={filters.location} onValueChange={(value) => handleFilterChange("location", value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Any location" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="Any location">Any location</SelectItem>
                      {filterOptions?.locations.map((location) => (
                        <SelectItem key={location} value={location}>
                          {location}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Sort by</Label>
                  <Select value={filters.sortBy} onValueChange={(value) => handleFilterChange("sortBy", value as any)}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="rating">Highest Rated</SelectItem>
                      <SelectItem value="experience">Most Experience</SelectItem>
                      <SelectItem value="reviews">Most Reviews</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-end">
                  <Button variant="outline" onClick={clearFilters} className="w-full bg-transparent">
                    Clear Filters
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Results */}
        {!mentors || mentors.length === 0 ? (
          <Card>
            <CardContent className="py-12 text-center">
              <Users className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-foreground mb-2">No Mentors Found</h3>
              <p className="text-muted-foreground mb-4">Try adjusting your search criteria or filters</p>
              <Button onClick={clearFilters}>Clear All Filters</Button>
            </CardContent>
          </Card>
        ) : (
          <>
            <div className="flex items-center justify-between mb-6">
              <p className="text-muted-foreground">
                Found {mentors.length} mentor{mentors.length !== 1 ? "s" : ""}
              </p>
            </div>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {mentors.map((mentor) => (
                <Card key={mentor.id} className="hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex items-start space-x-4">
                      <Avatar className="w-16 h-16">
                        <AvatarImage src={mentor.profileImage || "/placeholder.svg"} alt={mentor.name} />
                        <AvatarFallback>
                          {mentor.name
                            .split(" ")
                            .map((n) => n[0])
                            .join("")}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1 min-w-0">
                        <CardTitle className="text-lg truncate">{mentor.name}</CardTitle>
                        <CardDescription className="text-sm">
                          {mentor.jobTitle} at {mentor.company}
                        </CardDescription>
                        <div className="flex items-center mt-2 space-x-4 text-sm text-muted-foreground">
                          <div className="flex items-center">
                            <Star className="w-4 h-4 mr-1 fill-yellow-400 text-yellow-400" />
                            {mentor.rating} ({mentor.reviewCount})
                          </div>
                          <div className="flex items-center">
                            <Briefcase className="w-4 h-4 mr-1" />
                            {mentor.experience}y exp
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <p className="text-sm text-muted-foreground line-clamp-3">{mentor.bio}</p>

                      <div className="space-y-2">
                        <div className="flex items-center text-sm text-muted-foreground">
                          <MapPin className="w-4 h-4 mr-2" />
                          {mentor.location}
                        </div>
                      </div>

                      <div className="space-y-2">
                        <Label className="text-xs font-medium">Expertise</Label>
                        <div className="flex flex-wrap gap-1">
                          {mentor.expertise.slice(0, 3).map((exp, index) => (
                            <Badge key={index} variant="secondary" className="text-xs">
                              {exp}
                            </Badge>
                          ))}
                          {mentor.expertise.length > 3 && (
                            <Badge variant="outline" className="text-xs">
                              +{mentor.expertise.length - 3} more
                            </Badge>
                          )}
                        </div>
                      </div>

                      <div className="pt-2">
                        <Link href={`/mentors/${mentor.id}`}>
                          <Button className="w-full">View Profile</Button>
                        </Link>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  )
}
