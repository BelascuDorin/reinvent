"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { trpc } from "@/lib/trpc"
import { Users, ArrowLeft, Calendar, Clock, Euro, Search } from "lucide-react"
import Link from "next/link"

export default function BrowsePage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  const [selectedMeeting, setSelectedMeeting] = useState<any>(null)
  const [bookingMessage, setBookingMessage] = useState("")
  const [isBookingDialogOpen, setIsBookingDialogOpen] = useState(false)

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: availableSlots, isLoading } = trpc.meeting.getAvailableSlots.useQuery({})

  const bookMeetingMutation = trpc.meeting.bookMeeting.useMutation({
    onSuccess: () => {
      setIsBookingDialogOpen(false)
      setBookingMessage("")
      setSelectedMeeting(null)
      router.push("/meetings?booked=true")
    },
  })

  const handleBookMeeting = () => {
    if (!selectedMeeting) return

    bookMeetingMutation.mutate({
      meetingId: selectedMeeting.id,
      message: bookingMessage,
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
              <h1 className="text-xl font-semibold text-foreground">Browse Mentors</h1>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Available Sessions</h2>
          <p className="text-muted-foreground">Find and book mentoring sessions with experienced professionals</p>
        </div>

        {/* Search and Filters */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle className="flex items-center">
              <Search className="w-5 h-5 mr-2" />
              Find Sessions
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label>Search by topic</Label>
                <Input placeholder="e.g., Software Engineering, Marketing" />
              </div>
              <div className="space-y-2">
                <Label>Date</Label>
                <Input type="date" />
              </div>
              <div className="space-y-2">
                <Label>Price Range</Label>
                <select className="w-full px-3 py-2 border border-input rounded-md bg-background">
                  <option value="">Any price</option>
                  <option value="free">Free only</option>
                  <option value="paid">Paid sessions</option>
                </select>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Available Sessions */}
        {!availableSlots || availableSlots.length === 0 ? (
          <Card>
            <CardContent className="py-12 text-center">
              <Calendar className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-foreground mb-2">No Sessions Available</h3>
              <p className="text-muted-foreground">
                Check back later for new mentoring sessions from our professionals
              </p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {availableSlots.map((slot) => (
              <Card key={slot.id} className="hover:shadow-lg transition-shadow">
                <CardHeader>
                  <CardTitle className="text-lg">{slot.title}</CardTitle>
                  <CardDescription>with {slot.mentorName}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {slot.description && <p className="text-sm text-muted-foreground">{slot.description}</p>}

                    <div className="space-y-2">
                      <div className="flex items-center text-sm text-muted-foreground">
                        <Calendar className="w-4 h-4 mr-2" />
                        {new Date(slot.date).toLocaleDateString("en-US", {
                          weekday: "long",
                          year: "numeric",
                          month: "long",
                          day: "numeric",
                        })}
                      </div>
                      <div className="flex items-center text-sm text-muted-foreground">
                        <Clock className="w-4 h-4 mr-2" />
                        {slot.startTime} - {slot.endTime}
                      </div>
                      <div className="flex items-center text-sm font-medium">
                        <Euro className="w-4 h-4 mr-2" />
                        {slot.price === 0 ? (
                          <span className="text-green-600">Free</span>
                        ) : (
                          <span>{slot.price} RON</span>
                        )}
                      </div>
                    </div>

                    <Dialog open={isBookingDialogOpen} onOpenChange={setIsBookingDialogOpen}>
                      <DialogTrigger asChild>
                        <Button className="w-full mt-4" onClick={() => setSelectedMeeting(slot)}>
                          Book Session
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Book Session</DialogTitle>
                          <DialogDescription>
                            You're about to book "{selectedMeeting?.title}" with {selectedMeeting?.mentorName}
                          </DialogDescription>
                        </DialogHeader>

                        <div className="space-y-4">
                          <div className="p-4 bg-muted rounded-lg">
                            <div className="space-y-2 text-sm">
                              <div className="flex items-center">
                                <Calendar className="w-4 h-4 mr-2" />
                                {selectedMeeting && new Date(selectedMeeting.date).toLocaleDateString()}
                              </div>
                              <div className="flex items-center">
                                <Clock className="w-4 h-4 mr-2" />
                                {selectedMeeting?.startTime} - {selectedMeeting?.endTime}
                              </div>
                              <div className="flex items-center font-medium">
                                <Euro className="w-4 h-4 mr-2" />
                                {selectedMeeting?.price === 0 ? "Free" : `${selectedMeeting?.price} RON`}
                              </div>
                            </div>
                          </div>

                          <div className="space-y-2">
                            <Label htmlFor="message">Message to Mentor (Optional)</Label>
                            <Textarea
                              id="message"
                              value={bookingMessage}
                              onChange={(e) => setBookingMessage(e.target.value)}
                              placeholder="Tell the mentor what you'd like to discuss or any specific questions you have..."
                              rows={3}
                            />
                          </div>

                          {selectedMeeting?.price > 0 && (
                            <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                              <p className="text-sm text-yellow-800">
                                <strong>Payment Required:</strong> This session costs {selectedMeeting.price} RON.
                                Payment will be processed after the mentor confirms your booking.
                              </p>
                            </div>
                          )}

                          <div className="flex gap-2">
                            <Button
                              onClick={handleBookMeeting}
                              disabled={bookMeetingMutation.isLoading}
                              className="flex-1"
                            >
                              {bookMeetingMutation.isLoading ? "Booking..." : "Confirm Booking"}
                            </Button>
                            <Button variant="outline" onClick={() => setIsBookingDialogOpen(false)}>
                              Cancel
                            </Button>
                          </div>
                        </div>
                      </DialogContent>
                    </Dialog>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
